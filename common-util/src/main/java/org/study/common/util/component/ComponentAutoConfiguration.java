package org.study.common.util.component;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ComponentAutoConfiguration {

    @ConditionalOnBean(RocketMQTemplate.class)
    @Bean
    public RocketMQSender rmqSender(RocketMQTemplate rocketMQTemplate){
        return new RocketMQSender(rocketMQTemplate);
    }
}
