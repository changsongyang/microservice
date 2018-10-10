package org.study.demo.provider.rmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.study.common.mq.consumer.Consumer;
import org.study.common.mq.message.CMessage;
import org.study.common.statics.constants.MsgEvent;
import org.study.common.util.utils.JsonUtil;
import org.study.demo.provider.biz.UserBiz;
import org.study.demo.provider.entity.User;

import java.util.Map;

public class RmqConsumer extends Consumer {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    UserBiz userBiz;

    @Override
    public <T> boolean handleMessage(CMessage<T> cMessage){
        boolean result;
        switch (cMessage.getMsgEvent()) {
            case MsgEvent.USER_REGISTER:
                //用以模拟用户注册成功之后异步通知的其他业务处理
                User user = (User) cMessage.getBody();
                logger.info("用户注册成功，已发送邮件通知：userId={} userName={}", user.getId(), user.getUserName());
                result = true;
                break;
            case MsgEvent.USER_UPDATE_EXT:
                //用以模拟用户信息更新成功之后的另一个重要业务处理步骤
                Map<String, String> map = (Map<String, String>) cMessage.getBody();
                logger.info("用户信息已更新成功，已发送邮件通知：Map={}", JsonUtil.toString(map));
                result = true;
                break;
            default:
                result = false;
                logger.error("unexpected msgEvent:{}", cMessage.getMsgEvent());
                break;
        }
        return result;
    }
}
