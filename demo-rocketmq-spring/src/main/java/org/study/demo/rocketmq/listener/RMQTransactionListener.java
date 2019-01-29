package org.study.demo.rocketmq.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.study.demo.rocketmq.consts.Const;
import org.study.demo.rocketmq.vo.OrderVo;

@RocketMQTransactionListener(txProducerGroup = Const.TX_PRODUCER_GROUP)
public class RMQTransactionListener implements RocketMQLocalTransactionListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        OrderVo vo = JSONObject.parseObject((byte[])msg.getPayload(), OrderVo.class);

        if(vo.getMsgType() == 20001){
            logger.info("20001 OrderVo = {} ", JSON.toJSONString(vo));
        }else if(vo.getMsgType() == 20002){
            logger.info("20002 execute with item OrderVo = {} ", JSON.toJSONString(vo));
        }

        return RocketMQLocalTransactionState.UNKNOWN;
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        OrderVo vo = JSONObject.parseObject((byte[])msg.getPayload(), OrderVo.class);

        if(vo.getMsgType() == 20001){
            logger.info("20001 OrderVo = {} ", JSON.toJSONString(vo));
        }else if(vo.getMsgType() == 20002){
            logger.info("20002 check with item OrderVo = {} ", JSON.toJSONString(vo));
        }

        return RocketMQLocalTransactionState.COMMIT;
    }
}
