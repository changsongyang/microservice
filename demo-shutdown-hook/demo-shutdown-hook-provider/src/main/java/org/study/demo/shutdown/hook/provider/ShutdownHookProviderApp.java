package org.study.demo.shutdown.hook.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ShutdownHookProviderApp {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ShutdownHookProviderApp.class);
        app.setRegisterShutdownHook(false);//取消SpringBoot的优雅停机注册
        app.run(args);
    }
}
