package org.study.service.timer.core.job.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQProperties;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;
import org.study.common.statics.exceptions.BizException;
import org.study.common.statics.vo.MessageVo;
import org.study.common.util.utils.DateUtil;
import org.study.common.util.utils.JsonUtil;
import org.study.common.util.utils.StringUtil;
import org.study.facade.timer.entity.ScheduleJob;
import org.study.starter.component.RocketMQSender;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Date;

@Component
public class JobNotifier {
    private Logger logger = LoggerFactory.getLogger(JobNotifier.class);

    @Autowired(required = false)
    private RocketMQSender rocketMQSender;

    @Autowired(required = false)
    private JmsTemplate jmsTemplate;
    @Autowired(required = false)
    private ActiveMQProperties activeMQProperties;

    @PostConstruct
    public void init(){
        if(activeMQProperties != null &&
                (StringUtil.isEmpty(activeMQProperties.getBrokerUrl()) || activeMQProperties.getBrokerUrl().indexOf("localhost") > 0)){
            logger.warn("---------------------------------> ActiveMQ使用本地内嵌Broker <---------------------------------");//提示作用
        }
    }


    public boolean notify(ScheduleJob scheduleJob) throws BizException {
        if(scheduleJob.getMqType().equals(ScheduleJob.MQ_TYPE_ROCKET)){
            return this.sendRocketMQ(scheduleJob);
        }else if(scheduleJob.getMqType().equals(ScheduleJob.MQ_TYPE_ACTIVE)){
            return this.sendActiveMQ(scheduleJob);
        }else{
            return false;
        }
    }

    private boolean sendRocketMQ(ScheduleJob scheduleJob){
        if(rocketMQSender == null){
            throw new BizException(BizException.BIZ_VALIDATE_ERROR, "无法发送RocketMQ信息，请检查RocketMQ相关配置信息");
        }

        try{
            MessageVo msg = new MessageVo();
            msg.setMsgType(0);
            msg.setTrxNo(this.buildTrxNo(scheduleJob));
            msg.setJsonParam(scheduleJob.getParamJson()==null ? "" : scheduleJob.getParamJson());

            String[] destArr = scheduleJob.getDestination().split(":");
            msg.setTopic(destArr[0]);

            String tags = "*";
            if(destArr.length > 1 && StringUtil.isNotEmpty(destArr[1])){
                tags = destArr[1];
            }
            msg.setTags(tags);

            return rocketMQSender.sendOne(msg);
        }catch(Throwable e){
            logger.error("发送RocketMQ消息时出现异常 jobGroup={} jobName={}", scheduleJob.getJobGroup(), scheduleJob.getJobName(), e);
            return false;
        }
    }

    private boolean sendActiveMQ(ScheduleJob scheduleJob){
        if(jmsTemplate == null){
            throw new BizException(BizException.BIZ_VALIDATE_ERROR, "无法发送ActiveMQ信息，请检查ActiveMQ相关配置信息");
        }

        try{
            MessageVo msg = new MessageVo();
            msg.setMsgType(0);
            msg.setTrxNo(this.buildTrxNo(scheduleJob));
            msg.setJsonParam(scheduleJob.getParamJson()==null? "" : scheduleJob.getParamJson());

            jmsTemplate.send(scheduleJob.getDestination(), new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createTextMessage(JsonUtil.toString(msg));
                }
            });
            return true;
        }catch(Throwable e){
            logger.error("发送ActiveMQ消息时出现异常 jobGroup={} jobName={}", scheduleJob.getJobGroup(), scheduleJob.getJobName(), e);
            return false;
        }
    }

    private String buildTrxNo(ScheduleJob scheduleJob){
        return scheduleJob.getJobGroup() + "_" + scheduleJob.getJobName() + "_" + DateUtil.formatDateTime(new Date());
    }
}
