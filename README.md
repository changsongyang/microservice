# microservice 微服务学习项目

#### 项目介绍
微服务学习模块，目前包括 SpringBoot 2.4、Dubbo 2.6.2、Mybatis、RocketMQ 4.3.1、Redis、Quartz 2.2.1 整合的一套微服务架构，使用Redisson作为redis的客户端，简化redis的使用，使用curator作为zookeeper的客户端，使用其分布式锁，后续继续完善考虑加入全链路跟踪、Nacos配置中心、ElasticSearch等内容。  <br/>


#### 整体项目介绍
common-parent &nbsp;&nbsp;&nbsp;&nbsp; 是所有模块的父pom，用以维护所有模块及第三方依赖包的版本等内容  </br>
common-static &nbsp;&nbsp;&nbsp;&nbsp; 是公用的静态基础模块，用以定义一些静态的内容，如：Enum、Constant、Exception、POJO、Annotation 等  <br/>
common-util &nbsp;&nbsp;&nbsp;&nbsp; 是公用的工具类，提供常用工具类和组件，如：StringUtil、JsonUtil，或者封装curator、redis组件等  <br/>
common-rocketmq &nbsp;&nbsp;&nbsp;&nbsp; 是使用RocketMQ来发送、消费消息的模块，在RocketMQ原生API上加了一层封装，方便业务开发使用  <br/>
common-service &nbsp;&nbsp;&nbsp;&nbsp; 主要用以Dubbo的服务提供方(provider)使用，定义常用的依赖包，MybatisDao等  <br/>

timer-provider &nbsp;&nbsp;&nbsp;&nbsp; 是以Quartz+Mysql为核心的简易版的分布式调度中心，可动态的增删查改定时任务，可集群部署、负载均衡、故障转移  <br/>

demo-provider &nbsp;&nbsp;&nbsp;&nbsp; Dubbo服务提供者样例模块，用以演示如何使用Dubbo进行服务发布，同时，演示了与 Mybatis、RocketMQ 的整合  <br/>
demo-restful &nbsp;&nbsp;&nbsp;&nbsp; 提供REST接口的web项目，用以演示：Dubbo服务消费者样例模块、RocketMQ发送和消费、Timer定时任务的操作 等等  <br/>
demo-nacos-config &nbsp;&nbsp;&nbsp;&nbsp; 用以演示使用 springCloud config + nacos 作为配置中心的样例  <br/>
demo-nacos-dubbo &nbsp;&nbsp;&nbsp;&nbsp; 用以演示使用 nacos + dubbo 作为配置中心和服务注册中心的样例  <br/>
demo-rocketmq-spring &nbsp;&nbsp;&nbsp;&nbsp; 用以演示使用 nacos + rocketmq-spring 作为来生产消息、消费消息的样例  <br/>
demo-shutdown-hook &nbsp;&nbsp;&nbsp;&nbsp; 主要用以演示自定义shutdown hook的样例，使用自定义shutdown hook主要是为了解决在应用重启时datasource、rocketmqTemplate等已经关闭，但是外部请求还在进来，从而导致数据不一致或者数据库有更新但是没有发送消息的情况，解决方案就是先把dubbo提供者从注册中心注销，再关闭spring的ApplicationContext <br/>