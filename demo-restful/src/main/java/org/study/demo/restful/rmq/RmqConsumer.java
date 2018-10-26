package org.study.demo.restful.rmq;

import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.study.common.mq.consumer.Consumer;
import org.study.common.mq.message.CMessage;
import org.study.common.statics.constants.MsgEvent;
import org.study.common.util.component.ZKClient;
import org.study.common.util.utils.JsonUtil;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class RmqConsumer extends Consumer {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    ZKClient zkClient;
    private AtomicLong batchCount = new AtomicLong(0);

    @Override
    public <T> boolean handleMessage(CMessage<T> cMessage) throws Exception{
        boolean result;
        switch (cMessage.getMsgEvent()) {
            case MsgEvent.SINGLE_MSG_TEST:
                logger.info("“消费到单笔消息：{}", JsonUtil.toString(cMessage));
                result = true;
                break;
            case MsgEvent.BATCH_MSG_TEST:
                logger.info("“消费到批量消息：{}", JsonUtil.toString(cMessage));
                result = true;
                break;
            case MsgEvent.TRANS_MSG_TEST_1:
                logger.info("“消费到事务消息_1：{}", JsonUtil.toString(cMessage));
                result = true;
                break;
            case MsgEvent.TRANS_MSG_TEST_2:
                logger.info("“消费到事务消息_2：{}", JsonUtil.toString(cMessage));
                result = true;
                break;
            case MsgEvent.TIMER_SIMPLE_JOB:
                this.executeTimerJob(cMessage);
                result = true;
                break;
            case MsgEvent.TIMER_CRON_JOB:
                this.executeTimerJob(cMessage);
                result = true;
                break;
            default:
                result = false;
                logger.error("unexpected msgEvent:{}", cMessage.getMsgEvent());
                break;
        }
        if(cMessage.getMsgEvent() == MsgEvent.BATCH_MSG_TEST){
            logger.info("========> 批量消息条数："+batchCount.incrementAndGet());
        }
        return result;
    }

    public void executeTimerJob(CMessage cMessage) throws Exception{
        String path = cMessage.getKey();
        InterProcessSemaphoreMutex lock = zkClient.getShareLock(path);//使用不可重入锁，避免同一线程重入了
        if(lock.acquire(2, TimeUnit.SECONDS)) {
            try {
                logger.info("key={} 定时任务执行中....", cMessage.getKey());
                Thread.sleep(5 * 1000);
            } finally {
                lock.release();
            }
        }else{
            logger.info("key={} 定时任务已经在执行，不必重复执行", cMessage.getKey());
        }
    }
}
