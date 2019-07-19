package org.study.demo.timer.mq;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.study.common.statics.constants.MsgTopicAndTags;
import org.study.common.statics.vo.MessageVo;
import org.study.common.util.utils.JsonUtil;

@Component
public class RMQConsumeListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Component
    @RocketMQMessageListener(topic = MsgTopicAndTags.TOPIC_QUARTZ_TIMER, selectorExpression = MsgTopicAndTags.TAG_TIMER_CRON, consumeThreadMax = 1, consumerGroup = "simpleTimer_consume")
    public class simpleTimeConsumer implements RocketMQListener<MessageVo> {
        public void onMessage(MessageVo message) {
            logger.info("接收到simple消息 MessageVo = {}", JsonUtil.toString(message));
        }
    }

    @Component
    @RocketMQMessageListener(topic = MsgTopicAndTags.TOPIC_QUARTZ_TIMER, selectorExpression = MsgTopicAndTags.TAG_TIMER_CRON, consumeThreadMax = 1, consumerGroup = "cronTimer_consume")
    public class cornTimerConsumer implements RocketMQListener<MessageVo> {
        public void onMessage(MessageVo message) {
            logger.info("接收到cron消息 MessageVo = {}", JsonUtil.toString(message));        }
    }
}
