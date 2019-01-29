package org.study.demo.nacos.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class NacosDubboProviderApp {
    public static void main(String[] args) {
        SpringApplication.run(NacosDubboProviderApp.class, args);
//        new SpringApplicationBuilder(NacosDubboProviderApp.class).web(WebApplicationType.NONE).run(args);
    }
}
