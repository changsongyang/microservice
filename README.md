# microservice 微服务学习项目

#### 项目介绍
微服务学习模块，目前包括 SpringBoot 2.4、Dubbo、Mybatis、RocketMQ、Redis、Quartz 整合的一套微服务架构。  <br/>


#### 整体项目介绍
common-parent &nbsp;&nbsp;&nbsp;&nbsp; 是所有模块的父pom，用以维护所有模块即第三方依赖包的版本等内容  </br>
common-static &nbsp;&nbsp;&nbsp;&nbsp; 是公用的静态基础模块，用以定义一些静态的内容，如：Enum、Constant、Exception、POJO、Annotation 等  <br/>
common-util &nbsp;&nbsp;&nbsp;&nbsp; 是公用的工具类，提供常用工具类和组件，如：StringUtil、JsonUtil，或者封装curator、redis组件等  <br/>
common-rocketmq &nbsp;&nbsp;&nbsp;&nbsp; 是使用RocketMQ来发送、消费消息的模块，在RocketMQ原生API上加了一层封装，方便业务开发使用  <br/>
common-service &nbsp;&nbsp;&nbsp;&nbsp; 主要用以Dubbo的服务提供方(provider)使用，定义常用的依赖包，MybatisDao等  <br/>

timer-provider &nbsp;&nbsp;&nbsp;&nbsp; 是以Quartz+Mysql为核心的简易版的分布式调度中心，可动态的增删查改定时任务，可集群部署、负载均衡、故障转移  <br/>

demo-provider &nbsp;&nbsp;&nbsp;&nbsp; Dubbo服务提供者样例模块，用以演示如何使用Dubbo进行服务发布，同时，演示了与 Mybatis、RocketMQ 的整合  <br/>
demo-restful &nbsp;&nbsp;&nbsp;&nbsp; 提供REST接口的web项目，用以演示：Dubbo服务消费者样例模块、RocketMQ发送和消费、Timer定时任务的操作 等等  <br/>