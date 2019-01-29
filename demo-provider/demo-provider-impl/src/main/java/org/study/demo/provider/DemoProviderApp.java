package org.study.demo.provider;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
public class DemoProviderApp {
    public static void main(String[] args) {
        new SpringApplicationBuilder(DemoProviderApp.class).web(WebApplicationType.NONE).run(args);
    }
}