package org.study.demo.provider;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
@NacosPropertySource(dataId = "demo-provider", autoRefreshed = true)
public class DemoProviderApp {
    public static void main(String[] args) {
        new SpringApplicationBuilder(DemoProviderApp.class).web(WebApplicationType.NONE).run(args);
    }
}