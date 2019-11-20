package com.xpay.service.timer.job.listener;

import com.xpay.common.util.utils.JsonUtil;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.xpay.service.timer.biz.QuartzBiz;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chenyf on 2018/2/1.
 */
public class JobListener implements org.quartz.JobListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    QuartzBiz quartzBiz;

    /**
     * 返回当前监听器的名字，这个方法必须被写他的返回值；
     * 因为listener需要通过其getName()方法广播它的名称
     */
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 任务被触发前触发
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        logger.info("==>任务将要执行 "+context.getJobDetail().getKey().getGroup()+":"+context.getJobDetail().getKey().getName());
    }

    /**
     * 这个方法正常情况下不执行,但是如果当TriggerListener中的vetoJobExecution方法返回true时,那么执行这个方法.
     * 需要注意的是 如果这个方法被执行 那么jobToBeExecuted、jobWasExecuted这两个方法不会执行,因为任务被终止了嘛
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        logger.debug("==>任务被否决 "+context.getJobDetail().getKey().getGroup()+":"+context.getJobDetail().getKey().getName());
    }

    /**
     * 任务调度完成后触发
     */
    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException){
        String jobGroup = context.getJobDetail().getKey().getGroup();
        String jobName = context.getJobDetail().getKey().getName();
        logger.info("==> 任务执行完毕 "+jobGroup+":"+jobName);
        Trigger trigger = context.getTrigger();

        try{
            Trigger.TriggerState triggerState = context.getScheduler().getTriggerState(trigger.getKey());
            Map<String, Object> jobProperties = new HashMap<>(50);
            jobProperties.put("jobStatus", triggerState.name());
            jobProperties.put("lastExecuteTime", context.getScheduledFireTime());
            jobProperties.put("nextExecuteTime", context.getNextFireTime());
            logger.trace("==>更新任务执行结果, jobGroup={} jobName={} jobProperties={}", jobGroup, jobName, JsonUtil.toStringFriendly(jobProperties));
            quartzBiz.updateScheduleJobAfterExecuted(jobGroup, jobName, jobProperties);
        }catch(Exception e){
            String jobUniqueKey = jobGroup+":"+jobName;
            logger.error("==>更新任务执行结果过程中出现异常 "+jobUniqueKey, e);
        }
    }
}

