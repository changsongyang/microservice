package org.study.demo.rocketmq.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.study.common.statics.exceptions.BizException;

import javax.jms.ConnectionFactory;

@SpringBootConfiguration
public class AppConfig {
    @Bean
    public ActiveMQConnectionFactoryConfig activeMQConnectionFactoryConfig(){
        return new  ActiveMQConnectionFactoryConfig();
    }

    /**
     * 消息消费失败时重发策略
     *
     * @param connectionFactory
     * @return
     */
    @Bean
    public RedeliveryPolicy redeliveryPolicy(ConnectionFactory connectionFactory) {
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        //是否在每次尝试重新发送失败后,成倍数增长这个间隔时间
        redeliveryPolicy.setUseExponentialBackOff(true);
        //首次重发时间间隔,默认为1秒
        redeliveryPolicy.setInitialRedeliveryDelay(2000);
        //重发时间间隔
        redeliveryPolicy.setRedeliveryDelay(redeliveryPolicy.getInitialRedeliveryDelay());
        //成倍数增长间隔时间的倍数
        redeliveryPolicy.setBackOffMultiplier(2);
        //最大间隔时间
        redeliveryPolicy.setMaximumRedeliveryDelay(120000);//2分钟
        //最大重试次数
        redeliveryPolicy.setMaximumRedeliveries(9);

        if (connectionFactory instanceof JmsPoolConnectionFactory) {
            ActiveMQConnectionFactory amqConnectFactory = (ActiveMQConnectionFactory) ((JmsPoolConnectionFactory) connectionFactory).getConnectionFactory();
            amqConnectFactory.setRedeliveryPolicy(redeliveryPolicy);
        } else {
            throw new BizException("未预期的ConnectionFactory实例类型");
        }
        return redeliveryPolicy;
    }
}
