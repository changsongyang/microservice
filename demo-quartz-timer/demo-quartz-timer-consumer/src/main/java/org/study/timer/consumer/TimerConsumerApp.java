package org.study.timer.consumer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class TimerConsumerApp {
    public static void main(String[] args) {
        new SpringApplicationBuilder(TimerConsumerApp.class).run(args);
    }
}