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

```java
RestTemplate restTemplate = new RestTemplate();

```