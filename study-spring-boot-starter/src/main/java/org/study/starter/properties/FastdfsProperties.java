package com.xpay.starter.comp.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Author: Cmf
 * Date: 2019/11/13
 * Time: 19:02
 * Description:
 */
@ConfigurationProperties(prefix = "fastdfs")
public class FastdfsProperties {
    /**
     * tracker服务列表
     */
    private String trackerServers;
    /**
     * 连接超时时间
     */
    private int connectTimeoutInSeconds;
    /**
     * IO超时时间
     */
    private int networkTimeoutInSeconds;

    private String charset = "UTF-8";

    private boolean httpAntiStealToken = false;

    private String httpSecretKey;

    public String getTrackerServers() {
        return trackerServers;
    }

    public void setTrackerServers(String trackerServers) {
        this.trackerServers = trackerServers;
    }

    public int getConnectTimeoutInSeconds() {
        return connectTimeoutInSeconds;
    }

    public void setConnectTimeoutInSeconds(int connectTimeoutInSeconds) {
        this.connectTimeoutInSeconds = connectTimeoutInSeconds;
    }

    public int getNetworkTimeoutInSeconds() {
        return networkTimeoutInSeconds;
    }

    public void setNetworkTimeoutInSeconds(int networkTimeoutInSeconds) {
        this.networkTimeoutInSeconds = networkTimeoutInSeconds;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public boolean isHttpAntiStealToken() {
        return httpAntiStealToken;
    }

    public void setHttpAntiStealToken(boolean httpAntiStealToken) {
        this.httpAntiStealToken = httpAntiStealToken;
    }

    public String getHttpSecretKey() {
        return httpSecretKey;
    }

    public void setHttpSecretKey(String httpSecretKey) {
        this.httpSecretKey = httpSecretKey;
    }
}
