package com.gw.api.base.config;

import com.gw.api.base.filters.RequestFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Servlet;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
public class MvcFilterAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "joinpay.api.servlet-wrapper-filter.enabled", havingValue = "true")
    public RequestFilter requestFilter(){
        return new RequestFilter();
    }

    /**
     * 注册请求过滤器
     * @return
     */
    @Bean
    @ConditionalOnBean(RequestFilter.class)
    public FilterRegistrationBean requestFilterRegistrationBean() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(requestFilter());
        registration.setEnabled(true); // 设置是否可用
        return registration;
    }
}
