package org.study.demo.rocketmq.listener;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.study.demo.rocketmq.vo.bizVo.OrderVo;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class RMQConsumeListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private long start = 0;
    AtomicLong messageCount = new AtomicLong(1);

    @Component
    @RocketMQMessageListener(topic = "my-topic", selectorExpression = "oneTag", consumeThreadMax = 1, consumerGroup = "my-topic_oneTag")
    public class oneTagConsumer implements RocketMQListener<OrderVo> {
        public void onMessage(OrderVo message) {
            if(start == 0){
                synchronized (RMQConsumeListener.class){
                    if(start == 0){
                        start = System.currentTimeMillis();
                    }
                }
            }
            long timeCost = (System.currentTimeMillis() - start)/1000;
            logger.info("timeCost={} messageCount={} OrderVo = {}", timeCost, messageCount.incrementAndGet(), JSON.toJSONString(message));
        }
    }

    @Component
    @RocketMQMessageListener(topic = "my-topic", selectorExpression = "oneTag", consumeThreadMax = 1, consumerGroup = "my-topic_oneTag2")
    public class oneTagConsumer2 implements RocketMQListener<OrderVo> {
        public void onMessage(OrderVo message) {
            if(start == 0){
                synchronized (RMQConsumeListener.class){
                    if(start == 0){
                        start = System.currentTimeMillis();
                    }
                }
            }
            long timeCost = (System.currentTimeMillis() - start)/1000;
            logger.info("timeCost={} messageCount={} OrderVo = {}", timeCost, messageCount.incrementAndGet(), JSON.toJSONString(message));
        }
    }

    @Component
    @RocketMQMessageListener(topic = "my-topic", selectorExpression = "batchTag", consumerGroup = "my-topic_batchTag")
    public class batchTagConsumer implements RocketMQListener<OrderVo> {
        public void onMessage(OrderVo message) {
            logger.info("OrderVo = {}", JSON.toJSONString(message));
        }
    }

    @Component
    @RocketMQMessageListener(topic = "my-topic", selectorExpression = "transTag", consumerGroup = "my-topic_transTag")
    public class transTagConsumer implements RocketMQListener<OrderVo> {
        public void onMessage(OrderVo message) {
            logger.info("OrderVo = {}", JSON.toJSONString(message));
        }
    }

    @Component
    @RocketMQMessageListener(topic = "my-topic", selectorExpression = "transTagItem", consumerGroup = "my-topic_transTagItem")
    public class transTagItemConsumer implements RocketMQListener<OrderVo> {
        public void onMessage(OrderVo message) {
            logger.info("OrderVo = {}", JSON.toJSONString(message));
        }
    }
}
