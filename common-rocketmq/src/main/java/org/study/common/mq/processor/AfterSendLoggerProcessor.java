package org.study.common.mq.processor;

import org.apache.rocketmq.client.producer.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.study.common.mq.conts.LoggerConst;
import org.study.common.mq.message.PMessage;
import org.study.common.util.utils.JsonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @description 记录日志的消息发送后处理器，也是默认处理器
 *     1、对于发送结果不是SEND_OK的消息，可以配置一个独立的日志文件来存储器日志，日志名称见{@link LoggerConst.ROCKETMQ_NOT_OK_LOGGER_NAME}
 * @author chenyf
 */
public class AfterSendLoggerProcessor extends AfterSendAbstractProcessor {
    protected final static String MESSAGE_SEPARATOR = "||";
    protected Logger logger = LoggerFactory.getLogger(AfterSendLoggerProcessor.class);
    protected Logger noOkLogger = LoggerFactory.getLogger(LoggerConst.ROCKETMQ_NOT_OK_LOGGER_NAME);


    /**
     * 消息发送后要执行的逻辑
     * @param msg
     * @param sendResult
     * @param ex
     */
    public <T> void executeAfterSend(PMessage<T> msg, SendResult sendResult, Throwable ex, long costMills){
        this.logPrint(msg, sendResult, ex, costMills);
    }

    /**
     * 批量消息发送后要执行的逻辑
     * @param topic
     * @param msgList
     * @param sendResult
     * @param ex
     */
    public <T> void executeAfterSend(String topic, List<PMessage<T>> msgList, SendResult sendResult, Throwable ex, long costMills){
        this.logPrint(topic, msgList, sendResult, ex, costMills);
    }

    /**
     * 日志打印发送的消息
     * @param sendResult
     * @param msg
     * @param sendResult
     * @param ex
     */
    public final <T> void logPrint(PMessage<T> msg, SendResult sendResult, Throwable ex, long costMills){
        if (super.isEnsureSendSuccess(sendResult)) {
            logger.debug("[RMQ_SEND_SUCCESS] Topic={} Tags={} Key={} Flag={} CostMills={}", msg.getTopic(), msg.getTags(), msg.getKey(), msg.getMsgFlag(), costMills);
        } else if(super.isSlaveFail(sendResult)){
            noOkLogger.warn("[RMQ_SEND_SLAVE_FAIL] SendResult={} CostMills={} PMessage={}", this.getSendStatus(sendResult),costMills, this.getPMessageLogPrint(msg));
        } else if(super.isRemoteTimeOut(sendResult, ex)){
            noOkLogger.warn("[RMQ_SEND_TIMEOUT] SendResult={} CostMills={} PMessage={}", this.getSendStatus(sendResult), costMills, this.getPMessageLogPrint(msg), ex);
        } else {
            //打印失败内容，消息内容用特殊的分隔符隔开，万一以后出现了特殊的异常需要爬取日志来恢复消息，也能方便解析
            noOkLogger.error("[RMQ_SEND_FAIL] SendStatus={} CostMills={} PMessage={}", this.getSendStatus(sendResult),costMills, this.getPMessageLogPrint(msg), ex);
        }
    }

    /**
     * 日志打印发送的消息
     * @param sendResult
     * @param msgList
     * @param sendResult
     * @param ex
     */
    public final <T> void logPrint(String topic, List<PMessage<T>> msgList, SendResult sendResult, Throwable ex, long costMills){
        if (super.isEnsureSendSuccess(sendResult)) {
            logger.debug("[RMQ_BATCH_SEND_SUCCESS] Topic={} Keys={} CostMills={}", topic, this.genKeyInfo(msgList), costMills);
        } else if(super.isSlaveFail(sendResult)){
            noOkLogger.warn("[RMQ_BATCH_SEND_SLAVE_FAIL] SendResult={} CostMills={} PMessageList={}", this.getSendStatus(sendResult), costMills, this.getPMessageLogPrint(msgList));
        } else if(super.isRemoteTimeOut(sendResult, ex)){
            noOkLogger.warn("[RMQ_BATCH_SEND_TIMEOUT] SendResult={} CostMills={} PMessageList={}", this.getSendStatus(sendResult), costMills, this.getPMessageLogPrint(msgList), ex);
        } else {
            //打印失败内容，消息内容用特殊的分隔符隔开，万一以后出现了特殊的异常需要爬取日志来恢复消息，也能方便解析
            noOkLogger.error("[RMQ_BATCH_SEND_FAIL] SendStatus={} CostMills={} PMessageList={}", this.getSendStatus(sendResult), costMills, this.getPMessageLogPrint(msgList), ex);
        }
    }

    /**
     * 生成KeyInfo用以日志打印，如果觉得此方法不使用，子类可以重写此方法
     * @param msgList
     * @param <T>
     * @return
     */
    protected <T> String genKeyInfo(List<PMessage<T>> msgList){
        List<String> keys = new ArrayList<>(msgList.size());
        for(PMessage message : msgList){
            keys.add(message.getKey());
        }
        return JsonUtil.toString(keys);
    }

    protected String getPMessageLogPrint(PMessage msg){
        return MESSAGE_SEPARATOR + JsonUtil.toStringFriendly(msg) + MESSAGE_SEPARATOR;
    }
    protected <T> String getPMessageLogPrint(List<PMessage<T>> msgList){
        return MESSAGE_SEPARATOR + JsonUtil.toStringFriendly(msgList) + MESSAGE_SEPARATOR;
    }

    protected String getSendStatus(SendResult sendResult){
        if(sendResult == null){
            return null;
        }else{
            return sendResult.getSendStatus().name();
        }
    }
}
