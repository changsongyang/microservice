package org.study.demo.rocketmq.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQConnectionFactoryCustomizer;

/**
 * 定制化配置ActiveMQ的ActiveMQConnectionFactory配置
 */
public class ActiveMQConnectionFactoryConfig implements ActiveMQConnectionFactoryCustomizer {

    public void customize(ActiveMQConnectionFactory factory){
        factory.setStatsEnabled(true);
    }
}
