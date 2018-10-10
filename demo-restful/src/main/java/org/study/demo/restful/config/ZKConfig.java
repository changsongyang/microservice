package org.study.demo.restful.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.study.common.util.component.ZKClient;

@SpringBootConfiguration
@ConfigurationProperties(prefix = "config.zookeeper")
public class ZKConfig {
    private String zkAddress;

    public String getZkAddress() {
        return zkAddress;
    }

    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    @Bean
    public ZKClient zkClient(){
        ZKClient zkClient = new ZKClient();
        zkClient.setUrls(zkAddress);
        return zkClient;
    }

}
