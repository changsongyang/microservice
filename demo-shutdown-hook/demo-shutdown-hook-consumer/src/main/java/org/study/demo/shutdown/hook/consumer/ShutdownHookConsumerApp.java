package org.study.demo.shutdown.hook.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ShutdownHookConsumerApp {
    public static void main(String[] args) {
        SpringApplication.run(ShutdownHookConsumerApp.class, args);
    }
}
