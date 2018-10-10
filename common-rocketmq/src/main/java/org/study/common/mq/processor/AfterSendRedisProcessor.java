package org.study.common.mq.processor;

import org.apache.rocketmq.client.producer.SendResult;
import org.redisson.api.RedissonClient;
import org.study.common.mq.conts.LoggerConst;
import org.study.common.mq.message.PMessage;
import org.study.common.statics.exceptions.BizException;
import org.study.common.util.utils.JsonUtil;

import javax.annotation.PostConstruct;
import java.util.List;

public class AfterSendRedisProcessor extends AfterSendLoggerProcessor {
    private RedissonClient redissonClient;
    /**
     * 可能失败的消息是否需要重新存储到Redis中，默认是TRUE
     */
    private boolean restoreFailMsgToRedis = true;
    /**
     * 可能失败的消息在Redis存储时的KEY
     */
    private String redisStoreKey = LoggerConst.REDIS_STORE_SEND_NOT_OK_KEY;


    /**
     * 初始化方法
     * @throws Exception
     */
    @PostConstruct
    public void init(){
        if(this.redissonClient == null){
            throw new BizException("redissonClient is null");
        }
    }

    /**
     * 消息发送后要执行的逻辑
     * @param msg
     * @param sendResult
     * @param ex
     */
    public <T> void executeAfterSend(PMessage<T> msg, SendResult sendResult, Throwable ex, long costMills){
        super.logPrint(msg, sendResult, ex, costMills);

        //1、如果Slave失败，那么Slave可能会保存消息失败，此时可把消息存入Redis中，把Redis充当了Slave的角色
        //2、如果出现超时情况，此时不能确定Broker端是接收消息成功还是失败，此时为保险起见，还是把消息放到Redis中
        if(restoreFailMsgToRedis && !super.isEnsureSendSuccess(sendResult)){
            try{
                redissonClient.getList(redisStoreKey).add(JsonUtil.toStringFriendly(msg));
            }catch(Throwable e){
                noOkLogger.error("[RMQ_STORE_TO_REDIS_ERROR] PMessage={}", super.getPMessageLogPrint(msg), e);
            }
        }
    }

    /**
     * 批量消息发送后要执行的逻辑
     * @param topic
     * @param msgList
     * @param sendResult
     * @param ex
     */
    public <T> void executeAfterSend(String topic, List<PMessage<T>> msgList, SendResult sendResult, Throwable ex, long costMills){
        super.logPrint(topic, msgList, sendResult, ex, costMills);
        if(restoreFailMsgToRedis && !super.isEnsureSendSuccess(sendResult)){
            try{
                redissonClient.getList(redisStoreKey).add(JsonUtil.toStringFriendly(msgList));
            }catch(Throwable e){
                noOkLogger.error("[RMQ_BATCH_STORE_TO_REDIS_ERROR] RMessageList={}", super.getPMessageLogPrint(msgList), e);
            }
        }
    }


    public void setRedissonClient(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public boolean isRestoreFailMsgToRedis() {
        return restoreFailMsgToRedis;
    }

    public void setRestoreFailMsgToRedis(boolean restoreFailMsgToRedis) {
        this.restoreFailMsgToRedis = restoreFailMsgToRedis;
    }

    public void setRedisStoreKey(String key){
        if(key == null || key.trim().length() == 0){
            return;
        }
        this.redisStoreKey = key;
    }

    public String getRedisStoreKey() {
        return redisStoreKey;
    }
}
