package org.study.demo.hot.deploy.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@SpringBootConfiguration
@ConfigurationProperties
public class CommonProperties {
    private String hotDeployFlag;

    public String getHotDeployFlag() {
        return hotDeployFlag;
    }

    public void setHotDeployFlag(String hotDeployFlag) {
        this.hotDeployFlag = hotDeployFlag;
    }
}
