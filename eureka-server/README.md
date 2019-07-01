### 服务注册中心
---
### 失效剔除
当服务实例出现内存溢出、网络故障灯原因使服务不能正常工作，而服务注册中心未收到 “服务下线”的请求。为了剔除这些无用服务，
`Eureka Server` 在启动的时候会创建一个定时任务，默认每隔一段时间（默认 60 秒），将当前清单中超时（默认为 90 秒）没有续约的服务
剔除出去。
---
### 自我保护
`Eureka Server` 在运行期间，会统计心跳失败的比例在15分钟之内是否低于 85 %，如果出现低于的情况（在单机下容易满足），`Eureka Server` 
会将当前的实力注册信息保护起来，让这些实例不会过期，尽可能保护这些注册信息。但是，在这段保护期间内若出现问题，那么客户端很容易拿到实际已经不存在
的服务实例，会出现调用失败的情况，所以客户端必须要有容错机制，比如请求重试、断路器等机制。

### 服务注册中心处理
所有的交互都是通过 `REST` 请求来发起的。第一层 key 为 `InstanceInfo` 中的 `appName` 属性，第二层 `Key` 存储实例名为 `InstanceInfo` 中的 `instanceId`
.属性

### 元数据
+ 元数据介绍
服务实例的元数据指的是 `Eureka` 客户端在向服务注册中心发送注册请求时，用来描述自身服务信息的对象，其中包含了一些标准化的元数据，比如服务名称、
实例名称、实例 `IP` 、实例端口 等用于服务治理的重要信息；以及一些用于负载均衡策略或是其他特殊用途的自定义元数据信息。
其中 `Map<String,String> metadata = new ConcurrentHashMap<String,String>();`是自定义的元数据信息，而其他成员变量则是标准化的元数据信息。
`SpringCloud` 的 `EurekaINstanceConfigBean` 对原生元数据对象做了一些配置优化处理。
可以通过 `eureka.instance.<properties>=<value>` 的格式对'标准化元数据'直接进行配置。而对于自定义元数据，可以通过 `eureka.instance.metadataMap
.<key>=<value>` 的格式来进行配置。eg：
`eureka.instace.metadataMap.zone=shanghai`
+ 实例名配置
实例名，即 `InstanceInfo` 中的 `instanceId` 参数，它是区分同一服务中不同实例的唯一标识，在 Netflix Eureka 的原生实现中，实例名采用主机名作为默认值，
这样的设置使得在同一主机上无法启动多个相同的服务实例。所以，在 Spring Cloud Eureka 的配置中，针对同一主机中启动多实例的情况，对实例名默认做了更为合理的扩展，
采用了一下默认规则
`${spring.cloud.client.hostnamne}:${spring.application.name}:${spring.application.instance_id:${server.port}}`
当设置了上下文时，需要更改上下文。为了安全考虑，有时也会修改 /info 和 /health 端点的原始路径
```yaml
management:
  context-path: /hello
eureka:
  instance:
    status-page-url-path: ${management.context-path}/${endpoints.info.path}
    health-check-url-path: ${management.context-path}/${endpoints.health.path}
```
当然，如果客户端应用以 HTTPS 的方式来暴露服务和监控端点时，相对路径的配置方式就无法满足需求了。所以 SpringCloud Eureka 还提供了绝对路径的配置参数
eg:
```yaml
eureka:
  instance:
    homePageUrl: https://${eureka.instance.hostname}/
    status-page-url-path: https://${eureka.instance.hostname}/${management.context-path}/${endpoints.info.path}
    health-check-url-path: https://${eureka.instance.hostname}/${management.context-path}/${endpoints.health.path}
```
当服务器的其他应用不能使用时，心跳连接仍为可用，但其实该服务实例是不可用的。所以设置 Eureka 客户端的健康监测交给 spring-boot-actuator 模块
的 /health端点，实现更全面的健康状态维护。
```yaml
eureka:
  client:
    healthcheck:
      enabled: true
```

### 通信协议
默认情况下，Eureka 使用 Jersey 和 XStream 配合 JSON 作为 Server 与 Client 之间的通信协议。