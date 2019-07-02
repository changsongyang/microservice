package org.study.starter.config;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.elasticsearch.client.RestHighLevelClient;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.study.starter.component.EsClient;
import org.study.starter.component.RedisClient;
import org.study.starter.component.RocketMQSender;

@Configuration
public class ComponentAutoConfiguration {

    @ConditionalOnClass(RocketMQTemplate.class)
    @ConditionalOnBean(RocketMQTemplate.class)
    @Bean
    public RocketMQSender rmqSender(RocketMQTemplate rocketMQTemplate){
        return new RocketMQSender(rocketMQTemplate);
    }

    @ConditionalOnClass(RestHighLevelClient.class)
    @ConditionalOnBean(RestHighLevelClient.class)
    @Bean
    public EsClient esClient(RestHighLevelClient restHighLevelClient){
        return new EsClient(restHighLevelClient);
    }

    @ConditionalOnClass(RedissonClient.class)
    @ConditionalOnBean(RedissonClient.class)
    @Bean
    public RedisClient redisClient(RedissonClient redissonClient){
        return new RedisClient(redissonClient);
    }
}
