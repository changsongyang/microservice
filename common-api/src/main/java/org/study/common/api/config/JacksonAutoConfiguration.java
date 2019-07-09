package org.study.common.api.config;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonAutoConfiguration {

    /**
     * 配置jackson
     * @return
     */
    @ConditionalOnProperty(name = "study.api.jackson-snake-case.enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return new Jackson2ObjectMapperBuilderCustomizer() {
            @Override
            public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
                //驼峰命名的参数会自动转为下划线分割的参数，下划线分割的参数会自动转为驼峰命名的参数，如： orderNo <--> order_no
                jacksonObjectMapperBuilder.propertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            }
        };
    }
}
