package org.study.starter.component;

import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.study.common.statics.exceptions.BizException;
import org.study.common.statics.vo.MessageVo;
import org.study.common.util.utils.JsonUtil;

import java.nio.charset.StandardCharsets;
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

        SendResult sendResult = rocketMQTemplate.syncSend(getDestination(msg.getTopic(), msg.getTags()), message);
        return SendStatus.SEND_OK.equals(sendResult.getSendStatus());
    }

    /**
     * 发送批量消息，适合同一个业务事件有多个业务系统需要做不同业务处理的时候使用
     * 注意：4.5.2版本下Broker端使用DledgerCommitLog模式时还不支持批量消息，会报 [CODE: 13 MESSAGE_ILLEGAL] 的异常，在常规的Master-Slave下可以
     * @param destination   目的地，如果只有topic，则只传topic名称即可，如果还有tags，则拼接成 topic:tags 的形式
     * @param msgList
     * @return
     */
    public boolean sendBatch(String destination, List<? extends MessageVo> msgList){
        try {
            long now = 0;
            boolean isDebugEnabled = log.isDebugEnabled();
            if(isDebugEnabled){
                now = System.currentTimeMillis();
            }

            List<Message> sprMsgList = new ArrayList<>(msgList.size());
            for(MessageVo msg : msgList){
                Message<byte[]> message = buildMessage(msg);
                sprMsgList.add(message);
            }
            SendResult sendResult = rocketMQTemplate.syncSend(destination, sprMsgList, 3000L);
            if(isDebugEnabled){
                log.debug("sendBatch message cost: {} ms, msgId:{}", (System.currentTimeMillis()-now), sendResult.getMsgId());
            }
            return SendStatus.SEND_OK.equals(sendResult.getSendStatus());
        } catch (Throwable e) {
            log.error("sendBatch failed. destination:{}, msgList:{} ", destination, JsonUtil.toString(msgList));
            throw new BizException("批量消息发送异常", e);
        }
    }

    /**
     * 发送批量消息，适合同一个业务事件有多个业务系统需要做不同业务处理的时候使用
     * 注意：4.5.1版本下Broker端使用DledgerCommitLog模式时还不支持批量消息，会报 [CODE: 13  DESC: the message is illegal, maybe msg body or properties length not matched] 的异常，在常规的Master-Slave下可以
     * @param msgList
     * @return
     */
    public boolean sendBatch(List<? extends MessageVo> msgList){
        try {
            long now = 0;
            boolean isDebugEnabled = log.isDebugEnabled();
            if(isDebugEnabled){
                now = System.currentTimeMillis();
            }

            List<org.apache.rocketmq.common.message.Message> rmsgList = new ArrayList<>(msgList.size());
            for(MessageVo msg : msgList){
                Message<byte[]> message = buildMessage(msg);
                String destination = getDestination(msg.getTopic(), msg.getTags());
                org.apache.rocketmq.common.message.Message rocketMsg = RocketMQUtil.convertToRocketMessage(destination, message);

                rmsgList.add(rocketMsg);
            }

            SendResult sendResult = rocketMQTemplate.getProducer().send(rmsgList);
            if(isDebugEnabled){
                log.debug("sendBatch message cost: {} ms, msgId:{}", (System.currentTimeMillis()-now), sendResult.getMsgId());
            }
            return SendStatus.SEND_OK.equals(sendResult.getSendStatus());
        } catch (Throwable e) {
            log.error("sendBatch failed. msgList:{} ", JsonUtil.toString(msgList));
            throw new BizException("批量消息发送异常", e);
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

        SendResult sendResult = rocketMQTemplate.sendMessageInTransaction(txProducerGroup, getDestination(msg.getTopic(), msg.getTags()), message, null);
        return SendStatus.SEND_OK.equals(sendResult.getSendStatus());
    }


    private String getDestination(String topic , String tags){
        return topic + ":" + tags;
    }

    private Message<byte[]> buildMessage(MessageVo msg){
        Message<byte[]> message = MessageBuilder.withPayload(JsonUtil.toString(msg).getBytes(StandardCharsets.UTF_8))
                .setHeader(MessageConst.PROPERTY_KEYS, msg.getTrxNo())
                .build();
        return message;
    }
}
