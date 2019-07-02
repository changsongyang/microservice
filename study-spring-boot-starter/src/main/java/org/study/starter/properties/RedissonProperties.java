package org.study.starter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.study.starter.enums.RedisClusterMode;

@ConfigurationProperties(prefix = "redis")
public class RedissonProperties {

    private RedisClusterMode clusterMode;
    private String urls;
    private String masterName;
    private String password;

    public RedisClusterMode getClusterMode() {
        return clusterMode;
    }

    public void setClusterMode(RedisClusterMode clusterMode) {
        this.clusterMode = clusterMode;
    }

    public String getUrls() {
        return urls;
    }

    public void setUrls(String urls) {
        this.urls = urls;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
