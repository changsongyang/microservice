package org.study.timer.provider.core.job.base;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.study.timer.provider.core.biz.InstanceStageBiz;
import org.study.timer.provider.core.biz.QuartzBiz;
import org.study.timer.provider.entity.ScheduleJob;

/**
 * Quartz的任务执行类：
 *     说明：
 *          1、此项目只是作为定时任务的触发器，而真正执行业务逻辑的方法或者类并不在本项目中，而是分散到各自的业务系统中，当定时任务触发，会通过发送消息的形式
 *             来通知各个业务进行相应的业务处理。
 *          2、对于不允许并发执行的任务，由各个业务系统自行处理，比如：用分布式锁机制，真正执行业务逻辑之前先去获取锁，如果获取不到，说明此任务正在执行中，就
 *             根据业务实际需求来选择直接确认此消息还是把消息重新入列
 *          3、如果当前应用在多台服务器上都进行了部署，那就需要开启集群模式，并且每台服务器的时间要保持一致，避免多台机器同时触发了任务
 *          4、Quartz在触发任务的时候，默认是每一次执行都新建一个Job实例对象，所以如果本类有属性需要从JobDataMap中注入，这是线程安全的
 * @author chenyf on 2018/2/1.
 */
public class JobExecutor implements Job {
    private Logger logger = LoggerFactory.getLogger(JobExecutor.class);

    @Autowired
    QuartzBiz quartzBiz;
    @Autowired
    InstanceStageBiz instanceStageBiz;

    @Override
    public void execute(JobExecutionContext var1) throws JobExecutionException {
        JobKey jobKey = var1.getJobDetail().getKey();

        //因不知Quartz是否能完全暂停住任务，为保险起见，此处再次校验
        if(! instanceStageBiz.isInitFinished()){
            logger.error("当前实例未完成初始化，不可触发任务，将忽略本次任务 jobGroup={} jobName={}", jobKey.getGroup(), jobKey.getName());
            return;
        }else if(instanceStageBiz.isStandByMode()){
            logger.error("当前实例挂起中，不可触发任务，将忽略本次任务 jobGroup={} jobName={}", jobKey.getGroup(), jobKey.getName());
            return;
        }

        ScheduleJob scheduleJob;
        try{
            scheduleJob = quartzBiz.getJobByName(jobKey.getGroup(), jobKey.getName());
        }catch (Throwable e){
            logger.error("JobExecutor_jobGroup={} jobName={} 获取ScheduleJob时发生异常", jobKey.getGroup(), jobKey.getName(), e);
            return;
        }

        if(scheduleJob != null){
            quartzBiz.notifyExecuteScheduleJob(scheduleJob);
        }else{
            throw new JobExecutionException("JobExecutor_jobGroup="+jobKey.getGroup()+"_jobName="+jobKey.getName()+"对应的定时任务对象(ScheduleJob)不存在");
        }
    }
}
