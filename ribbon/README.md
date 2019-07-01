### 服务消费者
Spring Cloud Ribbon 是一个基于 HTTP 和TCP 的客户端负载均衡工具，它基于 Netflix Ribbon 实现。
通过 Spring Cloud 的封装，可以让我们轻松地将面向服务的 REST 模板请求自动转换成可互换负载均衡的服务调用。

### 客户端负载均衡

### RestTemplate 详解

#### GET 请求
+ `getForEntity`的三种方法
    1. `getForEntity(String url,Class responseType,Object...urlVariables);`
```java
RestTemplate restTemplate = new RestTemplate();
ResponseEntity<String> responseEntity = restTemplate.getForEntity("http://HELLO-SERVICE/hello/{1}",
                String.class,"didi");
String body = responseEntity.getBody();
```
    2. `getForEntity(String url,Class responseType,Map urlVariables);`
```java
RestTemplate restTemplate = new RestTemplate();
Map<String,String> params = new HashMap();
params.put("name","Avicci");
ResponseEntity<String> responseEntity = restTemplate.getForEntity("http://HELLO-SERVICE/hello?name={name}",
                String.class,params);
```
    3. `getForEntity(Url url,Class responseType);`
```java
RestTemplate restTemplate = new RestTemplate();
UriComponents uriComponents = UriComponentsBuilder.fromUriString(
                "http://HELLO-SERVICE/hello?name={name}")
                .build()
                .expand("Bruce Lee")
                .encode();
URI uri = uriComponents.toUri();
String body = restTemplate.getForEntity(uri,String.class).getBody()
```
+ `getForObject` 该方法可以理解为对getForEntity的进一步封装，它通过 `HTTPMessageConvertExtractor` 对 `HTTP`
的请求响应体 nody 内容进行对象转换，实现请求直接返回包装好的对象内容。
```java
RestTemplate restTemplate = new RestTemplate();
String body = restTemplate.getForObject(uri,String.class);
//和上面一样，也是三种，其实就是对上面的方法进行了进一步的封装
```
#### POST请求
+ `postForEntity` 和 `getForEntity`类似。
```java
RestTemplate restTemplate = new RestTemplate();
User user = new User("Bruce Lee",33);
String body = restTemplate.postForEntity("http://HELLO-SERVICE/hello",user,String.class);
```
与上面的 `GET` 是类似的。
+ `postForObject` 和 `getForObject`类似

+ `postForLocation` 函数，该方法时间了以POST请求提交资源，并返回新资源的 URI ，比如下面的例子
```java
User user = new User("Bruce Lee",33);
URI responseURI = restTemplate.postForLocation("http://HELLO-SERVICE/hello",user);
```
    1. public URI postForLocation(String url, Object request, Object... urlVariables)
    2. public URI postForLocation(String url, Object request, Map<String, ?> urlVariables)
    3. public URI postForLocation(URI url, Object request)
#### PUT请求
+ put(String url,Object request,Object... urlVariables)
+ put(String url,Object request,Map urlVariables)
+ put(URI url,Object request)
```java
RestTemplate restTemplate = new RestTemplate();
Long id = 10000L;
User user = new User("Avicci",29);
restTemplate.put("http://USER-SERVICE/user/{1}",user,id);
```
### DELETE 请求
+ delete(String url,Object request,Object... urlVariables)
+ delete(String url,Object request,Map urlVariables)
+ delete(URI url)
```java
RestTemplate restTemplate = new RestTemplate();
Long id = 10000L;
restTemplate.delete("http://USER-SERVICE/user/{1}",id);
```
也有三种方法，其实底层就是通过更改不同的请求方式，复用的同一个方法

---
### `Ribbon` 源码分析
```java
public interface LoadBalancerClient {
    ServiceInstance choose(String var1);

    <T> T execute(String var1, LoadBalancerRequest<T> var2) throws IOException;

    URI reconstructURI(ServiceInstance var1, URI var2);
}
```
+ ServiceInstance choose(String var1)： 根据传入的服务名serviceId，从负载均衡器中挑一个对应服务的实例。
+ <T> T execute(String var1, LoadBalancerRequest<T> var2) throws IOException ：使用从负载均衡器中
挑出的服务实例来执行请求内容
+ URI reconstructURI(ServiceInstance var1, URI var2)： 为系统构建一个合适的 host：port 形式的URI。在
分布式系统中，我们使用逻辑上的服务名称作为host来构建 `URI` （替代服务实例的 host：port形式）进行请求，例如之前的
http://USER-SERVICE/user
观察LoadBalanceClient 发现其同包下的类，`LoadBalancerAutoConfiguration` 为实现客户端负载均衡器的自动化配置类。
查看源码可知
```java
@Configuration
@ConditionalOnClass({RestTemplate.class})
@ConditionalOnBean({LoadBalancerClient.class})
public class LoadBalancerAutoConfiguration {
    @LoadBalanced
    @Autowired(
        required = false
    )
    private List<RestTemplate> restTemplates = Collections.emptyList();

    public LoadBalancerAutoConfiguration() {
    }

    @Bean
    public SmartInitializingSingleton loadBalancedRestTemplateInitializer(final List<RestTemplateCustomizer> customizers) {
        return new SmartInitializingSingleton() {
            public void afterSingletonsInstantiated() {
                Iterator var2 = LoadBalancerAutoConfiguration.this.restTemplates.iterator();

                while(var2.hasNext()) {
                    RestTemplate restTemplate = (RestTemplate)var2.next();
                    Iterator var4 = customizers.iterator();

                    while(var4.hasNext()) {
                        RestTemplateCustomizer customizer = (RestTemplateCustomizer)var4.next();
                        customizer.customize(restTemplate);
                    }
                }

            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplateCustomizer restTemplateCustomizer(final LoadBalancerInterceptor loadBalancerInterceptor) {
        return new RestTemplateCustomizer() {
            public void customize(RestTemplate restTemplate) {
                List<ClientHttpRequestInterceptor> list = new ArrayList(restTemplate.getInterceptors());
                list.add(loadBalancerInterceptor);
                restTemplate.setInterceptors(list);
            }
        };
    }

    @Bean
    public LoadBalancerInterceptor ribbonInterceptor(LoadBalancerClient loadBalancerClient) {
        return new LoadBalancerInterceptor(loadBalancerClient);
    }
}
```
以下为 LoadBalancerInterceptor 拦截器如何将一个普通的 RestTemplate 变成客户端负载均衡的
```java
public class LoadBalancerInterceptor implements ClientHttpRequestInterceptor {
    private LoadBalancerClient loadBalancer;

    public LoadBalancerInterceptor(LoadBalancerClient loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException {
        URI originalUri = request.getURI();
        String serviceName = originalUri.getHost();
        return (ClientHttpResponse)this.loadBalancer.execute(serviceName, new LoadBalancerRequest<ClientHttpResponse>() {
            public ClientHttpResponse apply(ServiceInstance instance) throws Exception {
                HttpRequest serviceRequest = LoadBalancerInterceptor.this.new ServiceRequestWrapper(request, instance);
                return execution.execute(serviceRequest, body);
            }
        });
    }

    private class ServiceRequestWrapper extends HttpRequestWrapper {
        private final ServiceInstance instance;

        public ServiceRequestWrapper(HttpRequest request, ServiceInstance instance) {
            super(request);
            this.instance = instance;
        }

        public URI getURI() {
            URI uri = LoadBalancerInterceptor.this.loadBalancer.reconstructURI(this.instance, this.getRequest().getURI());
            return uri;
        }
    }
}
```
