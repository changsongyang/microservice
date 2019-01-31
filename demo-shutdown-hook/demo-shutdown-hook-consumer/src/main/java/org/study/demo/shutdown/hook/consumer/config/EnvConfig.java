package org.study.demo.shutdown.hook.consumer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope//加上此注解，当配置有更新的时候会自动更新当前类的实例对象
@SpringBootConfiguration
public class EnvConfig {
    
    @Value(value = "${callTimes}")
    private Long callTimes;

    public Long getCallTimes() {
        return callTimes;
    }

    public void setCallTimes(Long callTimes) {
        this.callTimes = callTimes;
    }
}
