package org.study.starter.component;

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
import org.study.common.statics.vo.MessageVo;
import org.study.common.util.utils.JsonUtil;

import java.util.ArrayList;
import java.util.List;

public class RocketMQSender {
    Logger log = LoggerFactory.getLogger(this.getClass());
    private RocketMQTemplate rocketMQTemplate;

    public RocketMQSender(RocketMQTemplate rocketMQTemplate){
        this.rocketMQTemplate = rocketMQTemplate;
    }

    /**
     * 发送单个消息
     * @param msg
     * @return
     */
    public boolean sendOne(MessageVo msg) {
        Message message = MessageBuilder.withPayload(msg).setHeader(MessageConst.PROPERTY_KEYS, msg.getTrxNo()).build();

        rocketMQTemplate.syncSend(getDestination(msg.getTopic(), msg.getTags()), message);
        return true;
    }

    /**
     * 发送批量消息，适合同一个业务事件有多个业务系统需要做不同业务处理的时候使用
     * 注意：4.5.1版本下Broker端使用DledgerCommitLog模式时还不支持批量消息，会报 [CODE: 13 MESSAGE_ILLEGAL] 的异常，在常规的Master-Slave下可以
     * @param topic
     * @param msgList
     * @return
     */
    public boolean sendBatch(String topic, List<? extends MessageVo> msgList){
        try {
            long now = System.currentTimeMillis();
            List<org.apache.rocketmq.common.message.Message> rmsgList = new ArrayList<>(msgList.size());

            for(MessageVo msg : msgList){
                msg.setTopic(topic);//批量消息只能是相同topic下的

                Message message = MessageBuilder.withPayload(msg)
                        .setHeader(MessageConst.PROPERTY_KEYS, msg.getTrxNo())
                        .build();

                String destination = getDestination(msg.getTopic(), msg.getTags());
                org.apache.rocketmq.common.message.Message rocketMsg = RocketMQUtil.convertToRocketMessage(
                        rocketMQTemplate.getObjectMapper(), rocketMQTemplate.getCharset(), destination, message);

                rmsgList.add(rocketMsg);
            }

            SendResult sendResult = rocketMQTemplate.getProducer().send(rmsgList);
            long costTime = System.currentTimeMillis() - now;
            log.debug("sendBatch message cost: {} ms, msgId:{}", costTime, sendResult.getMsgId());
            return sendResult.getSendStatus().equals(SendStatus.SEND_OK);
        } catch (Throwable e) {
            log.error("sendBatch failed. topic:{}, msgList:{} ", topic, JsonUtil.toString(msgList));
            throw new MessagingException(e.getMessage(), e);
        }
    }

    /**
     * 发送事务消息
     * @param txProducerGroup 生产者分组，需要当前应用有 @RocketMQTransactionListener(txProducerGroup = XXXX) 添加这个监听时才能使用
     * @param msg
     * @return
     */
    public boolean sendTrans(String txProducerGroup, MessageVo msg) {
        Message message = MessageBuilder.withPayload(msg).setHeader(MessageConst.PROPERTY_KEYS, msg.getTrxNo()).build();

        rocketMQTemplate.sendMessageInTransaction(txProducerGroup, getDestination(msg.getTopic(), msg.getTags()), message, null);
        return true;
    }


    private String getDestination(String topic , String tags){
        return topic + ":" + tags;
    }
}
