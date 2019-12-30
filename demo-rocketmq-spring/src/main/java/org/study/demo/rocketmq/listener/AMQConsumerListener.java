package org.study.demo.rocketmq.listener;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class AMQConsumerListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    AtomicLong messageCount = new AtomicLong(1);
    private long start = 0;

    @JmsListener(destination = "queue.name.ongTag", subscription = "receiveOngTagMessage", concurrency = "10-10")
    public void receiveOngTagMessage(Message message){
        ActiveMQTextMessage activeMQTextMessage = (ActiveMQTextMessage) message;
        String textMessage;
        try {
            textMessage = activeMQTextMessage.getText();
        } catch (Exception ex) {
            throw new RuntimeException("消息转换时发生异常！", ex);
        }

        if(start == 0){
            synchronized (RMQConsumeListener.class){
                if(start == 0){
                    start = System.currentTimeMillis();
                }
            }
        }
        long timeCost = (System.currentTimeMillis() - start)/1000;

        logger.info("接收到MQ消息 timeCost={} messageCount={} textMessage = {} ", timeCost, messageCount.incrementAndGet(), textMessage);
    }

    @JmsListener(destination = "Consumer.A.VirtualTopic.Orders", subscription = "receiveVTopicA", concurrency = "1-5")
    public void receiveVTopicA(Message message){
        ActiveMQTextMessage activeMQTextMessage = (ActiveMQTextMessage) message;
        String textMessage;
        try {
            textMessage = activeMQTextMessage.getText();
        } catch (Exception ex) {
            throw new RuntimeException("消息转换时发生异常！", ex);
        }

        logger.info("接收到MQ消息 textMessage = {} ", textMessage);
    }

    @JmsListener(destination = "Consumer.B.VirtualTopic.Orders", subscription = "receiveVTopicB", concurrency = "1-5")
    public void receiveVTopicB(Message message){
        ActiveMQTextMessage activeMQTextMessage = (ActiveMQTextMessage) message;
        String textMessage;
        try {
            textMessage = activeMQTextMessage.getText();
        } catch (Exception ex) {
            throw new RuntimeException("消息转换时发生异常！", ex);
        }
        logger.info("接收到MQ消息 textMessage = {} ", textMessage);

        //DEBUG
        if(true){
            throw new RuntimeException("测试消息消费ACK机制");
        }
    }
}
