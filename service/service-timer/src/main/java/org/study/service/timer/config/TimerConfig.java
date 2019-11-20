package org.study.service.timer.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@SpringBootConfiguration
@ConfigurationProperties(prefix = "timer")
public class TimerConfig {
    /**
     * 实例的命名空间，可用以区分不同的环境、IDC等，适合有'蓝绿发布'的场景
     */
    private String namespace = "default";
    /**
     * 实例状态检查间隔，不建议设置过长，因为会导致实例状态设置后太久才生效，也不建议设置过短，因为这会加重数据库负担
     */
    private int stageCheckMills = 5000;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public int getStageCheckMills() {
        return stageCheckMills;
    }

    public void setStageCheckMills(int stageCheckMills) {
        this.stageCheckMills = stageCheckMills;
    }
}
