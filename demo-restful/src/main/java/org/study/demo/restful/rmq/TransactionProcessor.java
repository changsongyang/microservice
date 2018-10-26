package org.study.demo.restful.rmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.study.common.mq.message.CMessage;
import org.study.common.mq.processor.LocalTransactionProcessor;
import org.study.common.statics.constants.MsgEvent;
import org.study.common.util.utils.JsonUtil;

public class TransactionProcessor extends LocalTransactionProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public <T> Boolean executeLocalTransaction(final CMessage<T> cMessage){
        Boolean result = null;
        switch (cMessage.getMsgEvent()) {
            case MsgEvent.TRANS_MSG_TEST_1:
                result = true;
                logger.info("到了执行本地事务_1：{}", JsonUtil.toString(cMessage));
                break;
            case MsgEvent.TRANS_MSG_TEST_2:
                result = false;
                logger.info("到了执行本地事务_2：{}", JsonUtil.toString(cMessage));
                break;
            default:
                logger.error("unexpected msgEvent:{}", cMessage.getMsgEvent());
                break;
        }
        return result;
    }

    public <T> Boolean checkLocalTransaction(final CMessage<T> cMessage){
        Boolean result = null;
        switch (cMessage.getMsgEvent()) {
            case MsgEvent.TRANS_MSG_TEST_1:
                result = true;
                logger.info("“检查本地事务_1：{}", JsonUtil.toString(cMessage));
                break;
            case MsgEvent.TRANS_MSG_TEST_2:
                result = false;
                logger.info("“检查本地事务_2：{}", JsonUtil.toString(cMessage));
                break;
            default:
                logger.error("unexpected msgEvent:{}", cMessage.getMsgEvent());
                break;
        }
        return result;
    }
}
