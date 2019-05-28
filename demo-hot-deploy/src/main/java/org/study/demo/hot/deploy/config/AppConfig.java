package org.study.demo.hot.deploy.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;
import org.study.common.util.component.RocketMQSender;
import org.study.common.util.component.ShutdownHook;

import javax.sql.DataSource;

/**
 * 要想使用热更新的Bean不能直接在application.properties文件中配置，而是要单独在这里配置，并且要加上 @RefreshScope 注解
 */
@SpringBootConfiguration
public class AppConfig {
    @Bean
    public ShutdownHook shutdownHook(){
        return new ShutdownHook();
    }

    @RefreshScope
    @Bean
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(dataSourceProperties.getUrl());
        dataSource.setUsername(dataSourceProperties.getUsername());
        dataSource.setPassword(dataSourceProperties.getPassword());
        dataSource.setDriverClassName(dataSourceProperties.getDriverClassName());
        dataSource.setInitialSize(dataSourceProperties.getInitialSize());
        dataSource.setMinIdle(dataSourceProperties.getMinIdle());
        dataSource.setMaxActive(dataSourceProperties.getMaxActive());
        dataSource.setMaxWait(dataSourceProperties.getMaxWait());
        dataSource.setValidationQuery(dataSourceProperties.getValidationQuery());
        dataSource.setValidationQueryTimeout(dataSourceProperties.getValidationQueryTimeOut());
        dataSource.setTestOnBorrow(dataSourceProperties.getTestOnBorrow());
        dataSource.setTestOnReturn(dataSourceProperties.getTestOnReturn());
        dataSource.setTestWhileIdle(dataSourceProperties.getTestWhileIdle());
        dataSource.setTimeBetweenEvictionRunsMillis(dataSourceProperties.getTimeBetweenEvictionRunsMillis());
        dataSource.setMinEvictableIdleTimeMillis(dataSourceProperties.getMinEvictableIdleTimeMillis());
        return dataSource;
    }

    @RefreshScope
    @Bean
    public DefaultMQProducer defaultMQProducer(RocketMQProperties rocketMQProperties) {
        RocketMQProperties.Producer producerConfig = rocketMQProperties.getProducer();
        String nameServer = rocketMQProperties.getNameServer();
        String groupName = producerConfig.getGroup();
        Assert.hasText(nameServer, "[rocketmq.name-server] must not be null");
        Assert.hasText(groupName, "[rocketmq.producer.group] must not be null");

        DefaultMQProducer producer = new DefaultMQProducer(groupName);
        producer.setNamesrvAddr(nameServer);
        producer.setSendMsgTimeout(producerConfig.getSendMessageTimeout());
        producer.setRetryTimesWhenSendFailed(producerConfig.getRetryTimesWhenSendFailed());
        producer.setRetryTimesWhenSendAsyncFailed(producerConfig.getRetryTimesWhenSendAsyncFailed());
        producer.setMaxMessageSize(producerConfig.getMaxMessageSize());
        producer.setCompressMsgBodyOverHowmuch(producerConfig.getCompressMessageBodyThreshold());
        producer.setRetryAnotherBrokerWhenNotStoreOK(producerConfig.isRetryNextServer());

        return producer;
    }

    @RefreshScope
    @Bean(destroyMethod = "destroy")
    public RocketMQTemplate rocketMQTemplate(RocketMQProperties rocketMQProperties, ObjectMapper rocketMQMessageObjectMapper) {
        RocketMQTemplate rocketMQTemplate = new RocketMQTemplate();
        rocketMQTemplate.setProducer(defaultMQProducer(rocketMQProperties));
        rocketMQTemplate.setObjectMapper(rocketMQMessageObjectMapper);
        return rocketMQTemplate;
    }

    @RefreshScope
    @Bean
    public RocketMQSender rmqSender(RocketMQTemplate rocketMQTemplate){
        return new RocketMQSender(rocketMQTemplate);
    }
}
