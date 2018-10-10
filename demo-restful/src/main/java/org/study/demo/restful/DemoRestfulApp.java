package org.study.demo.restful;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class DemoRestfulApp {
    public static void main(String[] args) {
        new SpringApplicationBuilder(DemoRestfulApp.class).web(true).run(args);
    }
}