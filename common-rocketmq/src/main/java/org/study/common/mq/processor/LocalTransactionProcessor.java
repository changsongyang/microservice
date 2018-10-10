package org.study.common.mq.processor;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.study.common.mq.message.CMessage;
import org.study.common.mq.util.MessageUtil;

/**
 * @description 本地事务处理器，子类需要通过重写doLocalTransaction来执行本地事务、通过重写checkTransaction来检查本地事务
 * @author chenyf
 */
public abstract class LocalTransactionProcessor implements TransactionListener {
    protected Logger logger = LoggerFactory.getLogger(LocalTransactionProcessor.class);

    public final LocalTransactionState executeLocalTransaction(final Message msg, final Object arg){
        CMessage cMessage;
        try{
            cMessage = MessageUtil.convertMessage(msg, false);
        }catch (Throwable ex){
            logger.error("[RMQ_EXECUTE_TRANS_CONVERT_ERROR] Message = {}", msg.toString(), ex);
            return LocalTransactionState.UNKNOW;
        }

        try{
            logger.debug("[RMQ_EXECUTE_TRANS_START] KEY={} MsgEvent={}", cMessage.getKey(), cMessage.getMsgEvent());
            Boolean isSuccess = this.executeLocalTransaction(cMessage);
            if(isSuccess == null){
                logger.debug("[RMQ_EXECUTE_TRANS_FINISH] KEY={} MsgEvent={} IsSuccess={}", cMessage.getKey(), cMessage.getMsgEvent(), isSuccess);
                return LocalTransactionState.UNKNOW;
            }else if(Boolean.TRUE.equals(isSuccess)){
                logger.debug("[RMQ_EXECUTE_TRANS_FINISH] KEY={} MsgEvent={} IsSuccess={}", cMessage.getKey(), cMessage.getMsgEvent(), isSuccess);
                return LocalTransactionState.COMMIT_MESSAGE;
            }else{
                logger.debug("[RMQ_EXECUTE_TRANS_FINISH] KEY={} MsgEvent={} IsSuccess={}", cMessage.getKey(), cMessage.getMsgEvent(), isSuccess);
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        }catch (Throwable ex){
            logger.error("[RMQ_EXECUTE_TRANS_EXCEPTION] KEY={} MsgEvent={}", cMessage.getKey(), cMessage.getMsgEvent(), ex);
            return LocalTransactionState.UNKNOW;
        }
    }

    public final LocalTransactionState checkLocalTransaction(final MessageExt msg){
        CMessage cMessage;
        try{
            cMessage = MessageUtil.convertMessage(msg, true);
        }catch (Throwable ex){
            logger.error("[RMQ_CHECK_TRANS_CONVERT_ERROR] Message = {}", msg.toString(), ex);
            return LocalTransactionState.UNKNOW;
        }

        try{
            logger.debug("[RMQ_CHECK_TRANS_START] KEY={} MsgEvent={}", cMessage.getKey(), cMessage.getMsgEvent());
            Boolean isSuccess = this.checkLocalTransaction(cMessage);
            if(isSuccess == null){
                logger.debug("[RMQ_CHECK_TRANS_FINISH] KEY={} MsgEvent={} IsSuccess={}", cMessage.getKey(), cMessage.getMsgEvent(), isSuccess);
                return LocalTransactionState.UNKNOW;
            }else if(Boolean.TRUE.equals(isSuccess)){
                logger.debug("[RMQ_CHECK_TRANS_FINISH] KEY={} MsgEvent={} IsSuccess={}", cMessage.getKey(), cMessage.getMsgEvent(), isSuccess);
                return LocalTransactionState.COMMIT_MESSAGE;
            }else{
                logger.debug("[RMQ_CHECK_TRANS_FINISH] KEY={} MsgEvent={} IsSuccess={}", cMessage.getKey(), cMessage.getMsgEvent(), isSuccess);
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        }catch (Throwable ex){
            logger.error("[RMQ_CHECK_TRANS_EXCEPTION] KEY={} MsgEvent={}", cMessage.getKey(), cMessage.getMsgEvent(), ex);
            return LocalTransactionState.UNKNOW;
        }
    }

    /**
     * 执行本地事务
     * 注意：
     *      1、事务成功时返回TRUE，事务失败时返回FALSE，事务状态未知时返回NULL
     *      2、LocalTransactionProcessor对象是可复用的，在executeLocalTransaction方法内部可根据msgEvent来判断是哪种消息，进而知道应该查询哪个地方的本地事务
     *
     * @param msg
     * @param <T>
     * @return
     */
    public abstract <T> Boolean executeLocalTransaction(final CMessage<T> msg);

    /**
     * 检查本地事务
     * 注意：
     *      1、事务成功时返回TRUE，事务失败时返回FALSE，事务状态未知时返回NULL
     *      2、LocalTransactionProcessor对象是可复用的，在checkLocalTransaction方法内部可根据msgEvent来判断是哪种消息，进而知道应该查询哪个地方的本地事务
     *
     * @param msg
     * @param <T>
     * @return
     */
    public abstract <T> Boolean checkLocalTransaction(final CMessage<T> msg);
}
