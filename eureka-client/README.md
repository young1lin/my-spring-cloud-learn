###服务提供者
需要确认 eureka.client.register-with-eureka=true
该值默认为 true,如果为false 则不会启动注册操作

###关于服务续约
eureka.instance.lease-renewal-interval-in-seconds=30 //用于定义服务续约任务的调用间隔时间，默认为30s
eureka.instance.lease-expiration-duration-in-seconds=90 // 用于定义服务失效时间，默认为90s

###获取服务
为了性能考虑，Eureka Server 会维护一份只读的服务清单来返回给客户端，同时该缓存清单每隔 30 秒更新一次获取服务是服务消费者的基础，所以必须确保
eureka.client.fetch-registry=true 参数没被修改成 false，该值默认为 true。如果希望修改缓存清单的更新时间，可以通过
eureka.client.registry-fetch-interval-secons=30参数进行修改。

###服务调用
服务消费者在获取服务清单后，通过服务名可以获得具体提供服务的实例名和该实例的元数据信息。在 Ribbon 中会默认采用轮询的方式进行调用，从而实现客户端
的负载均衡。

###关于访问实例的选择
Eureka 中有 Region 和 Zone 的概念，一个 Region 中可以包含多个 Zone，每个服务客户端需要被注册到 Zone 中，所以每个客户端对应一个 Region 和一个
Zone。在进行服务调用的时候，有限访问同处一个 Zone 中的服务提供方，若访问不到，就访问其他的 Zone。

###服务下线
