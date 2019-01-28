package org.study.demo.nacos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope//加上此注解，当配置有更新的时候会自动更新当前类的实例对象
@SpringBootConfiguration
public class EnvConfig {
    @Value(value = "${useLocalCache:false}")
    private boolean useLocalCache;
    @Value(value = "${localKey:cacheKey}")
    private String localKey;
    @Value(value = "${localValue:10}")
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
