package org.study.demo.provider.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.study.common.mq.NameServerAddress;
import org.study.common.mq.consumer.Consumer;
import org.study.common.mq.producer.Producer;
import org.study.common.statics.constants.MsgTopicAndTags;
import org.study.demo.provider.rmq.RmqConsumer;
import org.study.demo.provider.rmq.TransactionProcessor;

import java.util.HashMap;
import java.util.Map;

@SpringBootConfiguration
@ConfigurationProperties(prefix = "config.rocketmq")
public class RocketMQConfig {
    private String nameServerAddress;
    private String producerGroupName;
    private String consumerGroupName;

    public String getNameServerAddress() {
        return nameServerAddress;
    }

    public void setNameServerAddress(String nameServerAddress) {
        this.nameServerAddress = nameServerAddress;
    }

    public String getProducerGroupName() {
        return producerGroupName;
    }

    public void setProducerGroupName(String producerGroupName) {
        this.producerGroupName = producerGroupName;
    }

    public String getConsumerGroupName() {
        return consumerGroupName;
    }

    public void setConsumerGroupName(String consumerGroupName) {
        this.consumerGroupName = consumerGroupName;
    }


    @Bean
    public NameServerAddress nameServerAddress() {
        NameServerAddress nameServerAddress = new NameServerAddress();
        nameServerAddress.setAddresses(getNameServerAddress());
        return nameServerAddress;
    }

    /**
     * 消息发送者
     *
     * @return
     */
    @Bean
    public Producer producer() {
        Producer producer = new Producer();
        producer.setNameServerAddress(nameServerAddress());
        producer.setGroupName(getProducerGroupName());
        producer.setIsSupportTransaction(true);
        producer.setLocalTransactionProcessor(transactionProcessor());
        return producer;
    }

    /**
     * 本地事务处理器
     *
     * @return
     */
    @Bean
    public TransactionProcessor transactionProcessor() {
        TransactionProcessor processor = new TransactionProcessor();
        return processor;
    }

    /**
     * 消息接收者
     *
     * @return
     */
    @Bean
    public Consumer rmqConsumer() {
        Consumer consumer = new RmqConsumer();
        consumer.setNameServerAddress(nameServerAddress());
        consumer.setGroupName(getConsumerGroupName());
        Map<String, String> subscriptionMap = new HashMap<>();
        subscriptionMap.put(MsgTopicAndTags.TOPIC_USER_BIZ, MsgTopicAndTags.TAG_USER_SEND_EMAIL+"|"+MsgTopicAndTags.TAG_USER_UPDATE_EXT);
//        subscriptionMap.put(MsgTopicAndTags.TOPIC_TIMER_SCHEDULE, "*");

        consumer.setSubscriptionMap(subscriptionMap);
        return consumer;
    }
}
