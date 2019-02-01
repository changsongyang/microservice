package org.study.demo.shutdown.hook.provider.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.study.common.util.component.RmqSender;
import org.study.common.util.component.ShutdownHook;

@SpringBootConfiguration
public class AppConfig {
    @Bean
    public ShutdownHook shutdownHook(){
        return new ShutdownHook();
    }

    @Bean
    public RmqSender rmqSender(){
        return new RmqSender();
    }
}
