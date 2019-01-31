package org.study.demo.shutdown.hook.provider.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.study.demo.shutdown.hook.provider.hook.AppShutdownHook;

@SpringBootConfiguration
public class AppConfig {
    @Bean
    public AppShutdownHook shutdownHook(){
        return new AppShutdownHook();
    }
}
