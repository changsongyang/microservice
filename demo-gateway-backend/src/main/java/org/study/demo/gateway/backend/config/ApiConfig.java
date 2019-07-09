package org.study.demo.gateway.backend.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * API项目的配置类，需要在@SpringBootApplication中增加此模块的包扫描才能起作用
 * 配置方式如：@SpringBootApplication(scanBasePackages = {"com.gw.api.demo", "com.gw.common.api"})
 * @author: chenyf
 * @Date: 2018-12-15
 */
@SpringBootConfiguration
public class ApiConfig extends WebMvcConfigurerAdapter {

//    @Bean
//    public MyInterceptor myInterceptor(){
//        return new MyInterceptor();
//    }

//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(myInterceptor());
//    }
}
