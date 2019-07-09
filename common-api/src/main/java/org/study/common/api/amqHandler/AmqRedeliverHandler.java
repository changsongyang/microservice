package com.gw.api.base.amqHandler;

import com.gw.api.base.exceptions.ApiException;
import com.gw.api.base.utils.JsonUtil;
import com.gw.api.base.vo.CallBackResult;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

public class AmqRedeliverHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final int REDELIVER_FAIL_INNER_ERROR_CODE = 100;//重投失败的错误码

    public static final String RESEND_COUNTER_PROPERTY_KEY = "resendCounter";
    private int networkErrorRedeliver = 1;//网络异常时的最大重投次数
    private int maxRedeliver = 4;//最大重投次数
    private JmsTemplate jmsTemplate;

    public AmqRedeliverHandler(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public AmqRedeliverHandler(JmsTemplate jmsTemplate, int networkErrorRedeliver, int maxRedeliver) {
        this.jmsTemplate = jmsTemplate;
        if(networkErrorRedeliver > maxRedeliver){//超时重试次数不能大于最大重试次数
            networkErrorRedeliver = maxRedeliver;
        }
        this.networkErrorRedeliver = networkErrorRedeliver;
        this.maxRedeliver = maxRedeliver;
    }

    /**
     * @param activemqMessage
     * @param result
     * @return 如果成功进行了重新投递，就返回true，如果不需要重新投递，则返回false，如果重投失败，则会抛出ApiException异常
     * @throws ApiException 如果重新投递失败，则会抛出ApiException异常，并且其innerCode为 {@link #REDELIVER_FAIL_INNER_ERROR_CODE}
     */
    public boolean redeliverIfNeed(ActiveMQTextMessage activemqMessage, CallBackResult result) throws ApiException {
        boolean isNeedRedeliver = false;
        int redeliverCount = this.getRedeliverCount(activemqMessage);
        if(result.isSuccess() || result.isNoNeedRetry() || redeliverCount >= maxRedeliver){
            return false;
        }else if(result.isNetworkError() && redeliverCount <= networkErrorRedeliver){ //如果是网络异常，可允许重试
            isNeedRedeliver = true;
        }else if(result.isMchRetry()){ //商户希望重试
            isNeedRedeliver = true;
        }else{
            logger.info("不符合重试条件，中止重投 CallBackResult = {}", JsonUtil.toString(result));
            return false;
        }

        if(isNeedRedeliver){
            //这种方式，在应用重启或者网络有波动等情况下，有可能会重新入队列失败，而让业务端感觉是没有触发重投的情况，对于此种情况，可以有几种解决方式：
            // 1、因为回调商户这种需求，并不是所有业务都要求百分百的可靠性，对于一些可以忍受偶尔有消息丢失的业务，可直接不用理会
            // 2、可在回调的发起方重新发送一条回调消息
            // 3、业务提供反查接口，商户定时通过此反查接口来获取结果
            // 4、把入队列失败的消息放入redis/数据库，然后再由另一处程序拿取数据后重新放入MQ，这样可进一步降低消息丢失的可能性
            isNeedRedeliver = this.resendMessage(activemqMessage, redeliverCount);
        }

        return isNeedRedeliver;
    }

    private boolean resendMessage(ActiveMQTextMessage activeMessage, int resendCount){
        try{
            String msg = activeMessage.getText();
            jmsTemplate.send(activeMessage.getDestination(), new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    TextMessage textMessage = session.createTextMessage(msg);
                    textMessage.setIntProperty(RESEND_COUNTER_PROPERTY_KEY, resendCount + 1);
                    return textMessage;
                }
            });
            return true;
        }catch(Throwable e){
            ApiException exception = new ApiException("重新入队列失败", e);
            exception.innerCode(REDELIVER_FAIL_INNER_ERROR_CODE);
            throw exception;
        }
    }

    private int getRedeliverCount(ActiveMQMessage activeMessage){
        int redeliverCount = activeMessage.getRedeliveryCounter();
        try{
            Object obj = activeMessage.getObjectProperty(RESEND_COUNTER_PROPERTY_KEY);
            redeliverCount = obj==null ? redeliverCount : (redeliverCount + (int)obj);
        }catch (Throwable e){
            //出现异常应抛出去，不然，在异步消费时可能导致消息进入无休止的循环投递，而把MQ服务器打垮
            throw new ApiException("获取重投次数时出现异常", e);
        }
        return redeliverCount;
    }
}
