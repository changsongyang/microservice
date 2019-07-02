package org.study.starter.config;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.study.starter.component.RocketMQSender;

@ConditionalOnClass(RocketMQTemplate.class)
@AutoConfigureAfter(RocketMQAutoConfiguration.class)
@Configuration
public class RMQAutoConfiguration {

    @ConditionalOnBean(RocketMQTemplate.class)
    @Bean
    public RocketMQSender rmqSender(RocketMQTemplate rocketMQTemplate){
        return new RocketMQSender(rocketMQTemplate);
    }

}
