package org.study.demo.rocketmq.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
public class AppConfig {
    @Bean
    public ActiveMQConnectionFactoryConfig activeMQConnectionFactoryConfig(){
        return new  ActiveMQConnectionFactoryConfig();
    }
}
