package com.xpay.service.timer.job.base;

import com.xpay.common.statics.exceptions.BizException;
import com.xpay.common.statics.dto.rmq.MsgDto;
import com.xpay.common.util.utils.DateUtil;
import com.xpay.common.util.utils.StringUtil;
import com.xpay.facade.timer.entity.ScheduleJob;
import com.xpay.starter.comp.component.RMQSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JobNotifier {
    private Logger logger = LoggerFactory.getLogger(JobNotifier.class);

    @Autowired
    private RMQSender rmqSender;


    public boolean notify(ScheduleJob scheduleJob) throws BizException {
        if (scheduleJob.getMqType().equals(ScheduleJob.MQ_TYPE_ROCKET)) {
            return this.sendRocketMQ(scheduleJob);
        } else {
            logger.error("没有匹配到MQ组件，消息通知失败！ jobGroup={} jobName={}", scheduleJob.getJobGroup(), scheduleJob.getJobName());
            return false;
        }
    }

    private boolean sendRocketMQ(ScheduleJob scheduleJob) {
        if (rmqSender == null) {
            throw new BizException(BizException.BIZ_INVALIDATE, "无法发送RocketMQ信息，请检查RocketMQ相关配置信息");
        }

        try {
            MsgDto msg = new MsgDto();
            msg.setEventType(0);
            msg.setTrxNo(this.buildTrxNo(scheduleJob));
            msg.setJsonParam(scheduleJob.getParamJson() == null ? "" : scheduleJob.getParamJson());

            String[] destArr = scheduleJob.getDestination().split(":");
            msg.setTopic(destArr[0]);

            String tags = "*";
            if (destArr.length > 1 && StringUtil.isNotEmpty(destArr[1])) {
                tags = destArr[1];
            }
            msg.setTags(tags);

            return rmqSender.sendOne(msg);
        } catch (Throwable e) {
            logger.error("发送RocketMQ消息时出现异常 jobGroup={} jobName={}", scheduleJob.getJobGroup(), scheduleJob.getJobName(), e);
            return false;
        }
    }

    private String buildTrxNo(ScheduleJob scheduleJob) {
        return scheduleJob.getJobGroup() + "_" + scheduleJob.getJobName() + "_" + DateUtil.formatDateTime(new Date());
    }
}
