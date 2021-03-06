# microservice 微服务学习项目

#### 项目介绍
微服务学习模块，包括 SpringBoot 2.1.x、Dubbo 2.7.1、Mybatis、RocketMQ 4.5.x、Redis、Quartz 2.3.x、Nacos Config、ElasticSearch整合的一套微服务架构，后续继续完善考虑加入全链路跟踪等内容。  <br/>


#### 整体项目介绍
common-parent &nbsp;&nbsp;&nbsp;&nbsp; 是所有模块的父pom，用以维护所有模块及第三方依赖包的版本等内容  </br>
common-statics &nbsp;&nbsp;&nbsp;&nbsp; 是公用的静态基础模块，用以定义一些静态的内容，如：Enum、Constant、Exception、Annotation、POJO等  <br/>
common-util &nbsp;&nbsp;&nbsp;&nbsp; 是公用的工具类，提供常用工具类和组件，如：StringUtil、JsonUtil等  <br/>
common-service &nbsp;&nbsp;&nbsp;&nbsp; 主要用以Dubbo的服务提供方(provider)使用，定义常用的依赖包，MybatisDao等  <br/>
demo-elasticsearch &nbsp;&nbsp;&nbsp;&nbsp; 演示elasticsearch的常规查询操作  <br/>
demo-nacos-config &nbsp;&nbsp;&nbsp;&nbsp; 用以演示使用 springCloud config + nacos 作为配置中心的样例  <br/>
demo-nacos-dubbo &nbsp;&nbsp;&nbsp;&nbsp; 用以演示使用 nacos + dubbo 作为配置中心和服务注册中心的样例  <br/>
demo-rocketmq-spring &nbsp;&nbsp;&nbsp;&nbsp; 用以演示使用 nacos + rocketmq-spring 作为来生产消息、消费消息的样例  <br/>
demo-quartz-timer  &nbsp;&nbsp;&nbsp;&nbsp; 简单封装了Quartz，使用Quartz + RocketMQ + Dubbo的方式来提供定时任务中心，任务触发时会往RocketMQ发送一条消息，消费端接收到消息之后即可进行业务处理  <br/>
demo-shutdown-hook &nbsp;&nbsp;&nbsp;&nbsp; 主要用以演示自定义shutdown hook的样例，使用自定义shutdown hook主要是为了解决在应用重启时datasource、rocketmqTemplate等已经关闭，但是外部请求还在进来，从而导致数据不一致或者数据库有更新但是没有发送消息的情况，解决方案就是先把dubbo提供者从注册中心注销，再关闭spring的ApplicationContext，同时，此样例也随demo一同演示了nacos+druid+mybatis+rocketmq联合使用的情况 <br/>
demo-hot-deploy &nbsp;&nbsp;&nbsp;&nbsp; 用以演示使用 nacos 来进行dataSource热更新、RocketMQ热更新 的样例  <br/>
study-spring-boot-starter &nbsp;&nbsp;&nbsp;&nbsp; 自动装配模块，主要是封装一些常规组件的操作，如：redis、rocketmq、elasticsearch、mailer等等，此模块作用有二：一是提供常用组件的封装类，降低这些组件的使用门槛；二是实现组件的自动装配，方便使用

