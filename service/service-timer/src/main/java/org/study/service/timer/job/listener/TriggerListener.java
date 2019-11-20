package com.xpay.service.timer.job.listener;

import com.xpay.common.util.utils.JsonUtil;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.xpay.service.timer.biz.QuartzBiz;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chenyf on 2018/2/2.
 */
public class TriggerListener implements org.quartz.TriggerListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    QuartzBiz quartzBiz;

    /**
     * 监听器名称
     * @return
     */
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 被调度时触发，和它相关的 org.quartz.jobDetail 即将执行。
     * 该方法优先vetoJobExecution()执行
     */
    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        logger.debug("==>触发器即将触发任务 "+trigger.getKey().getGroup()+":"+trigger.getKey().getName());
    }

    /**
     * 可根据实体业务情况来决定否决job，返回true时表示不执行当前任务，返回false时和它相关的 org.quartz.jobDetail 将被执行。
     * @param trigger
     * @param context
     * @return
     */
    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        logger.debug("==>触发器判断是否忽略此次触发 "+trigger.getKey().getGroup()+":"+trigger.getKey().getName());
        return false;
    }

    /**
     * 处理misfire的
     */
    @Override
    public void triggerMisfired(Trigger trigger) {
        //只要是被认定为misfire的任务在有机会时(获取到线程资源或者重启应用)都会被重新触发，然后各个任务再根据各自配置的策略来做出相应的处理
        logger.info("==>触发misfire任务,当前策略是：更新nextExecuteTime "+trigger.getKey().getGroup()+":"+trigger.getKey().getName());
        String jobGroup = trigger.getKey().getGroup();
        String jobName = trigger.getKey().getName();

        Map<String, Object> jobProperties = new HashMap<>(50);
        jobProperties.put("nextExecuteTime", trigger.getFireTimeAfter(new Date()));
        logger.trace("==>更新任务相关属性, jobGroup={} jobName={} jobProperties={}", jobGroup, jobName, JsonUtil.toStringFriendly(jobProperties));
        quartzBiz.updateScheduleJobAfterMisfire(jobGroup, jobName, jobProperties);
    }

    /**
     * job任务执行完毕时触发
     */
    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction var3) {
        logger.debug("==>触发器完成任务触发 "+trigger.getKey().getGroup()+":"+trigger.getKey().getName());
    }
}
