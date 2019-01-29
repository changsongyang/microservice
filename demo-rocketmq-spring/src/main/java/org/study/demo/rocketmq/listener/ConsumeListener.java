package org.study.demo.rocketmq.listener;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.study.demo.rocketmq.vo.OrderVo;

@Component
public class ConsumeListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Component
    @RocketMQMessageListener(topic = "my-topic", selectorExpression = "oneTag", consumerGroup = "my-topic_oneTag")
    public class oneTagConsumer implements RocketMQListener<OrderVo> {
        public void onMessage(OrderVo message) {
            logger.info("OrderVo = {}", JSON.toJSONString(message));
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
