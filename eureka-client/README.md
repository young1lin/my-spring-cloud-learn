### 服务提供者
需要确认 `eureka.client.register-with-eureka=true`
该值默认为 true,如果为false 则不会启动注册操作

### 关于服务续约
`eureka.instance.lease-renewal-interval-in-seconds=30` //用于定义服务续约任务的调用间隔时间，默认为30s
`eureka.instance.lease-expiration-duration-in-seconds=90` // 用于定义服务失效时间，默认为90s

### 获取服务
为了性能考虑，Eureka Server 会维护一份只读的服务清单来返回给客户端，同时该缓存清单每隔 30 秒更新一次获取服务是服务消费者的基础，所以必须确保
`eureka.client.fetch-registry=true` 参数没被修改成 false，该值默认为 true。如果希望修改缓存清单的更新时间，可以通过
`eureka.client.registry-fetch-interval-secons=30`参数进行修改。

### 服务调用
服务消费者在获取服务清单后，通过服务名可以获得具体提供服务的实例名和该实例的元数据信息。在 Ribbon 中会默认采用轮询的方式进行调用，从而实现客户端
的负载均衡。

###关于访问实例的选择
Eureka 中有 Region 和 Zone 的概念，一个 Region 中可以包含多个 Zone，每个服务客户端需要被注册到 Zone 中，所以每个客户端对应一个 Region 和一个
Zone。在进行服务调用的时候，有限访问同处一个 Zone 中的服务提供方，若访问不到，就访问其他的 Zone。

### 服务下线

---
### Region 和 Zone
在 `DiscoveryClient` 类中发现很多被不建议使用的方法，其中方法都是由 `EndpointUtils` 类提供，其中 `getServiceUrlsMapFromConfig()`
有 Region 和 Zone 的定义。
#### Region
可以通过`eureka.client.region`来定义
+ 由此可以看出，一个微服务应用只可以属于一个 Region
```java
public static String getRegion(EurekaClientConfig clientConfig){
    String region = clientConfig.getRegion();
    if(region == null){
        region = DEFEAULT_REGION;
    }`
    region = region.trim().toLowerCase();
    return region;
}
```
#### Zone
+ `eureka.client.serviceUrl.defaultZone` 设置 defaultZone.如果要指定 Zone（个人认为就是各个服务注册中心）。`eureka.client.availability-zones`
用来指定 Zone。Region 和 Zone 为一对多关系。
```java
public String[] getAvailabilityZones(String region) {
    String value = (String)this.availabilityZones.get(region);
    if (value == null) {
        value = "defaultZone";
    }
    return value.split(",");
}
```
当我们在微服务应用中使用 Ribbon 来实现服务调用时，对于 Zone 的设置可以再负载均衡实现区域亲和特性：Ribbon 的默认策略会优先访问同客户端处于一个 Zone
中的服务端实例，没有就其他 Zone 找。结合 Zone 属性定义，可以有效设计出对区域性故障的容错集群。
#### serviceUrls
获取了 Reion 和 Zone 的信息之后，才开始真正加载 Eureka Server 的具体地址。它根据传入的参数按一定算法确定加载位于哪一个 Zone 配置的 serviceUrls。
```java
in myZoneOffset = getZoneOffset(instanceZone,preferSameZone,availZones);
String zone = availZones[myZoneOffset];
List<String> serviceUrls = clientConfig.getEurekaServerServiceUrls(zone);

/**
* 该方法是 InstanceInfoReplicator 执行的定时任务，
*/
public void run() {
    boolean var6 = false;

    ScheduledFuture next;
    label53: {
        try {
            var6 = true;
            this.discoveryClient.refreshInstanceInfo();
            Long dirtyTimestamp = this.instanceInfo.isDirtyWithTime();
            if (dirtyTimestamp != null) {
                this.discoveryClient.register();
                this.instanceInfo.unsetIsDirty(dirtyTimestamp);
                var6 = false;
            } else {
                var6 = false;
            }
            break label53;
        } catch (Throwable var7) {
            logger.warn("There was a problem with the instance info replicator", var7);
            var6 = false;
        } finally {
            if (var6) {
                ScheduledFuture next = this.scheduler.schedule(this, (long)this.replicationIntervalSeconds, TimeUnit.SECONDS);
                this.scheduledPeriodicRef.set(next);
            }
        }

        next = this.scheduler.schedule(this, (long)this.replicationIntervalSeconds, TimeUnit.SECONDS);
        this.scheduledPeriodicRef.set(next);
        return;
    }

    next = this.scheduler.schedule(this, (long)this.replicationIntervalSeconds, TimeUnit.SECONDS);
    this.scheduledPeriodicRef.set(next);
}
/**
*  这一行，真正触发调用住的地方就在这里，以 `REST` 请求的范式进行的 .同时，在发起注册请求的时候，传入了一个 `com.netflix.appinfo.InstanceInfo`对象，该对象就是注册时
*  客户端给服务端的服务的元数据
*/
boolean register() throws Throwable {
        logger.info("DiscoveryClient_" + this.appPathIdentifier + ": registering service...");

        EurekaHttpResponse httpResponse;
        try {
            httpResponse = this.eurekaTransport.registrationClient.register(this.instanceInfo);
        } catch (Exception var3) {
            logger.warn("{} - registration failed {}", new Object[]{"DiscoveryClient_" + this.appPathIdentifier, var3.getMessage(), var3});
            throw var3;
        }

        if (logger.isInfoEnabled()) {
            logger.info("{} - registration status: {}", "DiscoveryClient_" + this.appPathIdentifier, httpResponse.getStatusCode());
        }

        return httpResponse.getStatusCode() == 204;
    }
```

### 服务注册
在服务治理框架中，通常都会构建一个注册中心，每个服务单元向注册中心登记自己提供的服务，将主机与端口号、版本号、通信协议等一些附加信息告知注册中心，
注册中心按服务名分类组织服务清单。双层Map保存，Map<String,Map<String,Object>>,第一层 Map key 为服务名，第二层Map key 为具体服务的实例名。
