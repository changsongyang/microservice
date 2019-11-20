package org.study.service.timer.job.base;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;
import org.study.common.statics.enums.TimeUnitEnum;
import org.study.common.statics.exceptions.BizException;
import org.study.common.statics.pojos.ServiceResult;
import org.study.facade.timer.entity.ScheduleJob;

import java.util.Date;
import java.util.List;

/**
 * @author chenyf on 2017/3/10.
 *
 * 说明：
 *     1、定时任务其实分为两个东西：trigger、jobDetail，其中trigger描述何时触发、怎样触发，jobDetail描述这是一个什么样的job，有哪些要传递的参数
 *     2、Quartz本身的设计是一个jobDetail可以有多个trigger，而一个trigger只能有一个jobDetail的，但是为了简单方便，在此类中的方法都是设计为成一个trigger只有
 *     一个jobDetail，一个jobDetail也只有一个trigger，添加任务时会同时添加trigger和jobDetail，修改时也会同时修改，删除时也会同时删除，并且两者的taskGroup、taskName是一样的
 *
 */
@Component
public class JobManager {
    private Logger logger = LoggerFactory.getLogger(JobManager.class);
    /**
     * 这个SchedulerFactoryBean是spring整合Quartz的对象，通过这个对象来对Quartz进行操作
     */
    @Autowired
    SchedulerFactoryBean schedulerFactoryBean;


    public void pauseInstance(){
        try{
            schedulerFactoryBean.getScheduler().standby();
        }catch(Throwable ex){
            logger.error("暂停实例失败", ex);
            throw new BizException(ex);
        }
    }

    public void resumeInstance(){
        try{
            schedulerFactoryBean.getScheduler().start();
        }catch(Throwable ex){
            throw new BizException(ex);
        }
    }

    /**
     * 添加一个新的任务，如果添加成功，会返回一个开始时间
     * @param scheduleJob
     * @return
     */
    public Date addJob(ScheduleJob scheduleJob) throws SchedulerException {
        if(checkJobExist(scheduleJob)){
            throw new BizException(BizException.BIZ_VALIDATE_ERROR, "jobGroup="+scheduleJob.getJobGroup()+",jobName="+scheduleJob.getJobName()+"的任务已存在！");
        }

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobDetail jobDetail = JobBuilder
                .newJob(JobExecutor.class)
                .withIdentity(scheduleJob.getJobName(), scheduleJob.getJobGroup())
                //当没有trigger关联的时候是否保留jobDetail，为false表示不保留，让ScheduleJob来保留就好了
                .storeDurably(false)
                .build();

        Trigger trigger = genTrigger(scheduleJob);
        Date startTime = scheduler.scheduleJob(jobDetail, trigger);

        if(startTime == null){
            return null;
        }else{
            Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
            scheduleJob.setJobStatus(triggerState.name());
            scheduleJob.setNextExecuteTime(scheduler.getTrigger(trigger.getKey()).getNextFireTime());
            return startTime;
        }
    }

    /**
     * 更新任务的触发trigger
     * @param scheduleJob
     * @return
     */
    public Date rescheduleJob(ScheduleJob scheduleJob) throws SchedulerException {
        if(! checkJobExist(scheduleJob)){
            throw new BizException(BizException.BIZ_VALIDATE_ERROR, "任务不在定时计划中，无法重新安排");
        }
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        TriggerKey triggerKey = TriggerKey.triggerKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());

        //获取trigger
        Trigger trigger = genTrigger(scheduleJob);
        //按新的Trigger重新设置job执行
        Date result = scheduler.rescheduleJob(triggerKey, trigger);
        if(result == null){
            return null;
        }else{
            Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
            scheduleJob.setJobStatus(triggerState.name());
            scheduleJob.setNextExecuteTime(result);
            return result;
        }
    }

    /**
     * 暂停某个任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    public void pauseJob(String jobGroup, String jobName) throws SchedulerException, BizException {
        ScheduleJob scheduleJob = new ScheduleJob(jobGroup, jobName);
        if(! checkJobExist(scheduleJob)){
            throw new BizException(BizException.BIZ_VALIDATE_ERROR, "任务不在定时计划中，无法暂停");
        }

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        scheduler.pauseJob(jobKey);
    }

    /**
     * 恢复某个任务，返回任务状态
     * @param jobGroup
     * @param jobName
     * @return
     */
    public String resumeJob(String jobGroup, String jobName) throws SchedulerException, BizException {
        ScheduleJob scheduleJob = new ScheduleJob(jobGroup, jobName);
        if(! checkJobExist(scheduleJob)){
            throw new BizException(BizException.BIZ_VALIDATE_ERROR, "任务不在定时计划中，无法恢复");
        }
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        TriggerKey triggerKey = TriggerKey.triggerKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        scheduler.resumeJob(jobKey);
        Trigger.TriggerState triggerState = scheduler.getTriggerState(triggerKey);
        return triggerState.name();
    }

    /**
     * 立即执行某任务一次
     * @param jobGroup
     * @param jobName
     * @return
     */
    public void triggerJob(String jobGroup, String jobName) throws SchedulerException,BizException {
        ScheduleJob scheduleJob = new ScheduleJob(jobGroup, jobName);
        if(! checkJobExist(scheduleJob)){
            throw new BizException(BizException.BIZ_VALIDATE_ERROR, "任务不在定时计划中，无法执行");
        }

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        scheduler.triggerJob(jobKey);
    }

    /**
     * 删除某个任务，如果任务已不存在，则直接返回success
     * @param jobGroup
     * @param jobName
     * @return
     */
    public boolean deleteJob(String jobGroup, String jobName) throws SchedulerException {
        ScheduleJob scheduleJob = new ScheduleJob(jobGroup, jobName);

        if(! checkJobExist(scheduleJob)){
            return true;
        }

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        return scheduler.deleteJob(jobKey);
    }

    /**
     * 获得当前正在执行的任务
     * @return
     */
    public ServiceResult<List<ScheduleJob>> listRunningJob(){
        return ServiceResult.success();
    }

    /**
     * 根据jobGroup、jobName检查一个任务是否已经存在
     * @param scheduleJob
     * @return
     */
    public boolean checkJobExist(ScheduleJob scheduleJob) throws SchedulerException {
        if(scheduleJob == null){
            return false;
        }

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        //以 jobName 和 jobGroup 作为唯一key
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        //获取JobDetail
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        if(jobDetail != null){
            //已存在
            return true;
        }else{
            return false;
        }
    }

    private Trigger genTrigger(ScheduleJob scheduleJob){
        Trigger trigger;
        //用DailyTimeIntervalTrigger触发任务(此处不使用SimpleTrigger是因为其misfire机制不合理，如果应用宕机或重启，可能导致触发紊乱)
        if(scheduleJob.getJobType().equals(ScheduleJob.SIMPLE_JOB)){
            //表达式调度构建器，设置直接忽略错过的任务，因为错过的任务可以直接手动在管理后台执行
            DailyTimeIntervalScheduleBuilder scheduleBuilder = DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule().withMisfireHandlingInstructionDoNothing();
            //设置间隔时间
            if(scheduleJob.getIntervals() != null){
                if(scheduleJob.getIntervalUnit().equals(TimeUnitEnum.SECOND.getValue())){
                    scheduleBuilder.withIntervalInSeconds(scheduleJob.getIntervals());
                }else if(scheduleJob.getIntervalUnit().equals(TimeUnitEnum.MINUTE.getValue())){
                    scheduleBuilder.withIntervalInMinutes(scheduleJob.getIntervals());
                }else if(scheduleJob.getIntervalUnit().equals(TimeUnitEnum.HOUR.getValue())){
                    scheduleBuilder.withIntervalInHours(scheduleJob.getIntervals());
                }else{
                    throw new BizException("UnSupported interval TimeUnit: "+scheduleJob.getIntervalUnit());
                }
            }
            //设置重复次数
            if(scheduleJob.getRepeatTimes() != null){
                scheduleBuilder.withRepeatCount(scheduleJob.getRepeatTimes());
            }
            //生成一个TriggerBuilder
            TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger()
                    //用以生成Trigger的key
                    .withIdentity(scheduleJob.getJobName(), scheduleJob.getJobGroup())
                    .withSchedule(scheduleBuilder)
                    .withDescription(scheduleJob.getJobDescription());
            //设置开始、结束时间
            if(scheduleJob.getStartTime() != null){
                triggerBuilder.startAt(scheduleJob.getStartTime());
            }
            if(scheduleJob.getEndTime() != null){
                triggerBuilder.endAt(scheduleJob.getEndTime());
            }
            trigger = triggerBuilder.build();

            //按cronExpression表达式构建CronTrigger来触发任务
        }else if(scheduleJob.getJobType().equals(ScheduleJob.CRON_JOB)){
            //设置任务调度表达式
            //表达式调度构建器，设置直接忽略错过的任务，因为错过的任务可以直接手动在管理后台执行
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression()).withMisfireHandlingInstructionDoNothing();

            //生成一个TriggerBuilder
            TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger()
                    //用以生成Trigger的key
                    .withIdentity(scheduleJob.getJobName(), scheduleJob.getJobGroup())
                    .withSchedule(scheduleBuilder)
                    .withDescription(scheduleJob.getJobDescription());
            //设置开始、结束时间
            if(scheduleJob.getStartTime() != null){
                triggerBuilder.startAt(scheduleJob.getStartTime());
            }
            if(scheduleJob.getEndTime() != null){
                triggerBuilder.endAt(scheduleJob.getEndTime());
            }
            trigger = triggerBuilder.build();
        }else{
            throw new RuntimeException("未知的任务类型");
        }
        return trigger;
    }
}
