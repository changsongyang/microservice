package org.study.demo.provider.rmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.study.common.mq.message.CMessage;
import org.study.common.mq.processor.LocalTransactionProcessor;
import org.study.common.statics.constants.MsgEvent;
import org.study.demo.provider.biz.UserBiz;
import org.study.demo.provider.entity.User;

import java.math.BigDecimal;
import java.util.Map;

public class TransactionProcessor extends LocalTransactionProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    UserBiz userBiz;

    public <T> Boolean executeLocalTransaction(final CMessage<T> cMessage){
        Boolean result = null;
        switch (cMessage.getMsgEvent()) {
            case MsgEvent.USER_UNREGISTER:
                Long id = (Long) cMessage.getBody();

                User user = userBiz.getById(id);
                if(user != null){
                    result = userBiz.deleteById(id);
                    if(result){
                        logger.info("用户注销成功 userId={} userName={}", id, user.getUserName());
                    }
                }else{
                    result = true;
                }
                break;
            case MsgEvent.USER_UPDATE_EXT:
                Map<String, String> map = (Map<String, String>) cMessage.getBody();
                id = Long.valueOf(map.get("id"));
                Integer age = Integer.valueOf(map.get("age"));
                String address = map.get("address");
                Integer height = Integer.valueOf(map.get("height"));
                BigDecimal weight = BigDecimal.valueOf(Double.valueOf(map.get("weight")));
                userBiz.doUpdateUserExt(id, age, address, height, weight);
                logger.info("用户更新成功，已更新完毕：userId={}", id);
                result = true;
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
            case MsgEvent.USER_UNREGISTER:
                Long id = (Long) cMessage.getBody();
                result = userBiz.getById(id) == null;
                break;
            case MsgEvent.USER_UPDATE_EXT:
                Map<String, String> map = (Map<String, String>) cMessage.getBody();
                id = Long.valueOf(map.get("id"));
                if(id%2 != 0){//这个规则跟userBiz.doUpdateUserExt里面保持一致，方便校验
                    result = Boolean.FALSE;
                }else{
                    result = Boolean.TRUE;
                }
                break;
            default:
                logger.error("unexpected msgEvent:{}", cMessage.getMsgEvent());
                break;
        }
        return result;
    }
}
