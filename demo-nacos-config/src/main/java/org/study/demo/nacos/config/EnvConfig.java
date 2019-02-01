package org.study.demo.nacos.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 有加 @ConfigurationProperties 注解的配置文件，不用再加 @RefreshScope 注解，因为SpringBoot已经支持自动更新被@ConfigurationProperties注解的类
 */
@SpringBootConfiguration
@ConfigurationProperties
public class EnvConfig {
    private boolean useLocalCache;
    private String localKey;
    private Integer localValue;


    public boolean getUseLocalCache() {
        return useLocalCache;
    }

    public void setUseLocalCache(boolean useLocalCache) {
        this.useLocalCache = useLocalCache;
    }

    public String getLocalKey() {
        return localKey;
    }

    public void setLocalKey(String localKey) {
        this.localKey = localKey;
    }

    public Integer getLocalValue() {
        return localValue;
    }

    public void setLocalValue(Integer localValue) {
        this.localValue = localValue;
    }
}
