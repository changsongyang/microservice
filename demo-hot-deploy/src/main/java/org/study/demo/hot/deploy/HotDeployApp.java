package org.study.demo.hot.deploy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HotDeployApp {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(HotDeployApp.class);
        app.setRegisterShutdownHook(false);//取消SpringBoot的优雅停机注册
        app.run(args);
    }
}
