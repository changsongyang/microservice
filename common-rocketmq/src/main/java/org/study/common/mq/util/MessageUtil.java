package org.study.common.mq.util;

import org.apache.rocketmq.common.message.Message;
import org.study.common.mq.message.CMessage;
import org.study.common.mq.conts.MessageConst;
import org.study.common.mq.message.PMessage;
import org.study.common.util.utils.ClassUtil;
import org.study.common.util.utils.JsonUtil;
import org.study.common.util.utils.StringUtil;

import java.nio.charset.Charset;

public class MessageUtil {
    private final static String CHARSET = "utf-8";

    /**
     * 消息转换，把 PMessage 转换为 RocketMQ 的 Message
     * @param msg
     * @param <T>
     * @return
     */
    public static <T> Message convertMessage(PMessage<T> msg){
        Message message = new Message(msg.getTopic(), msg.getTags(), msg.getKey(), JsonUtil.toString(msg.getBody()).getBytes(Charset.forName(CHARSET)));

        message.putUserProperty(MessageConst.PROPERTY_MSG_BIZ_KEY, msg.getKey()==null?"":msg.getKey());
        if(StringUtil.isNotEmpty(msg.getMsgEvent())){
            message.putUserProperty(MessageConst.PROPERTY_MSG_EVENT, String.valueOf(msg.getMsgEvent()));
        }
        if(StringUtil.isNotEmpty(msg.getBody())){
            message.putUserProperty(MessageConst.PROPERTY_MSG_BODY_CLASS, msg.getBody().getClass().getName());
        }
        return message;
    }

    /**
     * 消息转换，把 RocketMQ 的 Message 转换为 CMessage，如果CMessage中body对应的Class在当前应用找不到，则会把body转换成String
     * @param msg
     * @param isMsgExt
     * @param <T>
     * @return
     */
    public static <T> CMessage convertMessage(Message msg, boolean isMsgExt){
        CMessage cMessage = new CMessage();
        cMessage.setTopic(msg.getTopic());
        cMessage.setTags(msg.getTags());
        cMessage.setKey(msg.getUserProperty(MessageConst.PROPERTY_MSG_BIZ_KEY));

        String msgEvent = msg.getUserProperty(MessageConst.PROPERTY_MSG_EVENT);
        if(StringUtil.isNotEmpty(msgEvent)){
            cMessage.setMsgEvent(Integer.valueOf(msgEvent));
        }
        String bodyClassName = msg.getUserProperty(MessageConst.PROPERTY_MSG_BODY_CLASS);
        if(StringUtil.isEmpty(bodyClassName)){
            cMessage.setBody(msg.getBody());
        }else{
            try{
                Class cls = ClassUtil.getClass(bodyClassName);
                cMessage.setBody(JsonUtil.toBean(msg.getBody(), cls));
            }catch (ClassNotFoundException e){
                cMessage.setBody(msg.getBody());
            }
        }
        return cMessage;
    }
}
