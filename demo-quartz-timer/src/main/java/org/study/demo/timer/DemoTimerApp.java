package org.study.demo.timer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class DemoTimerApp {
    public static void main(String[] args) {
        new SpringApplicationBuilder(DemoTimerApp.class).run(args);
    }
}