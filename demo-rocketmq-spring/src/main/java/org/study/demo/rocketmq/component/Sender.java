package org.study.demo.rocketmq.component;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.study.demo.rocketmq.vo.MessageVo;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class Sender {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发送单个消息
     * @param topic
     * @param tags
     * @param msgKey
     * @param msg
     * @return
     */
    public boolean sendOne(String topic, String tags, String msgKey, MessageVo msg) {
        Message message = MessageBuilder.withPayload(msg).setHeader(MessageConst.PROPERTY_KEYS, msgKey).build();

        rocketMQTemplate.syncSend(topic+":"+tags, message);
        return true;
    }

    /**
     * 发送批量消息
     * @param topic
     * @param tags
     * @param msgKey
     * @param msgList
     * @return
     */
    public boolean sendBatch(String topic, String tags, String msgKey, List<? extends MessageVo> msgList){
        String destination = topic + ":" + tags;
        try {
            long now = System.currentTimeMillis();
            List<org.apache.rocketmq.common.message.Message> rmsgList = new ArrayList<>();
            for(MessageVo vo : msgList){
                Message message = MessageBuilder.withPayload(vo).setHeader(MessageConst.PROPERTY_KEYS, msgKey).build();

                org.apache.rocketmq.common.message.Message rocketMsg = RocketMQUtil.convertToRocketMessage(rocketMQTemplate.getObjectMapper(),
                        rocketMQTemplate.getCharset(), destination, message);
                rmsgList.add(rocketMsg);
            }

            SendResult sendResult = rocketMQTemplate.getProducer().send(rmsgList);
            long costTime = System.currentTimeMillis() - now;
            log.debug("sendBatch message cost: {} ms, msgKey:{}, msgId:{}", costTime, msgKey, sendResult.getMsgId());
            return sendResult.getSendStatus().equals(SendStatus.SEND_OK);
        } catch (Throwable e) {
            log.error("sendBatch failed. destination:{}, msgList:{} ", destination, JSON.toJSONString(msgList));
            throw new MessagingException(e.getMessage(), e);
        }
    }

    /**
     * 发送事务消息
     * @param txProducerGroup
     * @param topic
     * @param tags
     * @param msgKey
     * @param msg
     * @return
     */
    public boolean sendTrans(String txProducerGroup, String topic, String tags, String msgKey, MessageVo msg) {
        Message message = MessageBuilder.withPayload(msg).setHeader(MessageConst.PROPERTY_KEYS, msgKey).build();

        rocketMQTemplate.sendMessageInTransaction(txProducerGroup, topic+":"+tags, message, null);
        return true;
    }
}
