package org.study.demo.rocketmq.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.study.common.util.component.RmqSender;

@SpringBootConfiguration
public class MQConfig {
    @Bean
    public RmqSender rmqSender(){
        return new RmqSender();
    }
}
