package org.study.common.mq.consumer;

import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.study.common.mq.NameServerAddress;
import org.study.common.mq.message.CMessage;
import org.study.common.mq.util.MessageUtil;
import org.study.common.statics.exceptions.BizException;
import org.study.common.util.utils.StringUtil;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;

/**
 * @description RocketMQ的消费者类,是push类型
 * @author chenyf on 2018-03-18
 */
public abstract class Consumer {
    private Logger logger = LoggerFactory.getLogger(Consumer.class);

    private String groupName;
    private NameServerAddress nameServerAddress;
    private boolean isOrderly = false;
    private boolean isPushConsume = true;//默认使用PUSH方式消费
    private Map<String, String> subscriptionMap;

    private DefaultMQPushConsumer pushConsumer;
    private DefaultMQPullConsumer pullConsumer;

    /**
     * 每次消费消息的条数
     */
    private int consumeMessageBatchMaxSize = 5;

    private int consumeThreadMin = 8;
    private int consumeThreadMax = 64;
    private ConsumeFromWhere consumeFromWhere;

    /**
     * 初始化的方法
     */
    @PostConstruct
    public void initConsumer() throws Exception{
        if(nameServerAddress == null){
            throw new BizException("nameServerAddress is null");
        }else if(StringUtil.isEmpty(this.groupName)){
            throw new BizException("groupName is empty");
        }else if(subscriptionMap == null || subscriptionMap.isEmpty()){
            throw new BizException("subscriptionMap is empty");
        }

        if(this.isPushConsume){
            this.initPushConsumer();
        }else{
            this.initPullConsumer();
        }
    }


    /**
     * 处理消息的方法，也即是处理业务逻辑的方法，子类通过重写此方法来实现自己的业务需求
     * @param cMessage
     * @return
     */
    protected abstract <T> boolean handleMessage(CMessage<T> cMessage) throws Exception;


    /**
     * 对象销毁之前的处理
     */
    @PreDestroy
    public void shutdownConsumer(){
        if(this.pushConsumer != null){
            this.pushConsumer.shutdown();
        }
        if(this.pullConsumer != null){
            this.pullConsumer.shutdown();
        }
    }

    private void initPushConsumer() throws Exception{
        this.pushConsumer = new DefaultMQPushConsumer(this.groupName);
        pushConsumer.setNamesrvAddr(nameServerAddress.getAddresses());
        pushConsumer.setSubscription(subscriptionMap);
        pushConsumer.setConsumeThreadMin(this.consumeThreadMin);
        pushConsumer.setConsumeThreadMax(this.consumeThreadMax);
        pushConsumer.setConsumeMessageBatchMaxSize(consumeMessageBatchMaxSize);

        if(consumeFromWhere == null){
            pushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        }else{
            pushConsumer.setConsumeFromWhere(consumeFromWhere);
        }

        if(this.isOrderly){
            OrderlyMessageListener listener = new OrderlyMessageListener();
            pushConsumer.registerMessageListener(listener);
        }else{
            MessageListenerConcurrently listener = new ConcurrentlyMessageListener();
            pushConsumer.registerMessageListener(listener);
        }

        //开启消费者，不然不会消费
        this.pushConsumer.start();
    }

    private void initPullConsumer(){
        //TODO 暂时不需要
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setNameServerAddress(NameServerAddress nameServerAddress) {
        this.nameServerAddress = nameServerAddress;
    }

    public Map<String, String> getSubscriptionMap() {
        return subscriptionMap;
    }

    public void setSubscriptionMap(Map<String, String> subscriptionMap) {
        this.subscriptionMap = subscriptionMap;
    }

    public void setOrderly(boolean orderly) {
        isOrderly = orderly;
    }

    public void setConsumeThreadMin(int consumeThreadMin) {
        this.consumeThreadMin = consumeThreadMin;
    }

    public void setConsumeThreadMax(int consumeThreadMax) {
        this.consumeThreadMax = consumeThreadMax;
    }

    public ConsumeFromWhere getConsumeFromWhere() {
        return consumeFromWhere;
    }

    public void setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {
        this.consumeFromWhere = consumeFromWhere;
    }

    public void addSubscription(String topic, String tags){
        try{
            if(isPushConsume){
                this.pushConsumer.subscribe(topic, tags);
            }else{
                throw new BizException("pull方式不支持动态添加topic订阅者");
            }
        }catch (Throwable e){
            throw new BizException("添加topic订阅失败", e);
        }
    }

    /**---------------------------------------------------------- 内部类，两种消息监听器 ---------------------------------------------------------------*/
    /**
     * 可并发消费(即无序)的消息监听器
     * 注意：
     *     1、最好不要使用批量消费，不然如果一批消息中有一部分成功，一部分失败，那么整批消息都会重新消费，那么对于已经成功的那部分消息，在业务代码处理的时候就不是很好处理了
     */
    class ConcurrentlyMessageListener implements MessageListenerConcurrently {
        @Override
        public ConsumeConcurrentlyStatus consumeMessage(final List<MessageExt> msgs, final ConsumeConcurrentlyContext context) {
            boolean isSuccess;
            int successCount = 0;
            for (MessageExt msg : msgs) {
                try{
                    CMessage cMessage = MessageUtil.convertMessage(msg, true);
                    long start = System.currentTimeMillis();
                    logger.debug("[RMQ_CONSUME_START] KEY={} MsgEvent={}", cMessage.getKey(), cMessage.getMsgEvent());
                    isSuccess = handleMessage(cMessage);
                    logger.debug("[RMQ_CONSUME_FINISH] KEY={} MsgEvent={} IsSuccess={} CostMills={}", cMessage.getKey(), cMessage.getMsgEvent(), isSuccess, System.currentTimeMillis()-start);
                }catch(Throwable e){
                    isSuccess = false;
                    logger.error("[RMQ_CONSUME_EXCEPTION] MessageExt = {}", msg.toString(), e);
                }
                if(isSuccess){
                    successCount ++;
                }else{
                    break;
                }
            }

            if(successCount > 0){ //只要有消费成功的消息，就返回CONSUME_SUCCESS，返回之后会根据ackIndex的值来确定要同步回Broker的offset值
                context.setAckIndex(successCount - 1);//可以确认消费成功的index
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }else{
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        }
    }

    /**
     * 有序消费的消息监听器
     * 注意：
     *     1、最好不要使用批量消费，不然如果一批消息中有一部分成功，一部分失败，那么整批消息都会重新消费，那么对于已经成功的那部分消息，在业务代码处理的时候就不是很好处理了
     */
    class OrderlyMessageListener implements MessageListenerOrderly {
        @Override
        public ConsumeOrderlyStatus consumeMessage(final List<MessageExt> msgs, final ConsumeOrderlyContext context) {
            boolean isSuccess = false;
            for (MessageExt msg : msgs) {
                try{
                    CMessage cMessage = MessageUtil.convertMessage(msg, true);
                    long start = System.currentTimeMillis();
                    logger.debug("[RMQ_ORDERLY_CONSUME_START] KEY={} MsgEvent={}", cMessage.getKey(), cMessage.getMsgEvent());
                    isSuccess = handleMessage(cMessage);
                    logger.debug("[RMQ_ORDERLY_CONSUME_FINISH] KEY={} MsgEvent={} IsSuccess={} CostMills={}", cMessage.getKey(), cMessage.getMsgEvent(), isSuccess, System.currentTimeMillis()-start);
                }catch(Throwable e){
                    isSuccess = false;
                    logger.error("[RMQ_ORDERLY_CONSUME_EXCEPTION] MessageExt = {}", msg.toString(), e);
                }
                if (isSuccess == false) {
                    break;
                }
            }

            if(isSuccess){
                return ConsumeOrderlyStatus.SUCCESS;
            }else{
                return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
            }
        }
    }
}
