package org.study.common.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.study.common.api.servlets.ServletRequestFilter;

import javax.servlet.Servlet;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
public class MvcFilterAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "study.api.servlet-wrapper-filter.enabled", havingValue = "true", matchIfMissing = false)
    public ServletRequestFilter servletRequestFilter(){
        return new ServletRequestFilter();
    }

    /**
     * 注册请求过滤器
     * @return
     */
    @Bean
    @ConditionalOnBean(ServletRequestFilter.class)
    public FilterRegistrationBean requestFilterRegistrationBean() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(servletRequestFilter());
        registration.setEnabled(true); // 设置是否可用
        return registration;
    }
}
