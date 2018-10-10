package org.study.timer.provider.biz;

import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.study.common.mq.message.PMessage;
import org.study.common.mq.producer.Producer;
import org.study.common.statics.exceptions.BizException;
import org.study.common.statics.pojos.PageParam;
import org.study.common.statics.pojos.PageResult;
import org.study.common.statics.pojos.ServiceResult;
import org.study.common.util.utils.StringUtil;
import org.study.timer.provider.dao.ScheduleJobDao;
import org.study.timer.provider.entity.ScheduleJob;
import org.study.timer.provider.job.base.JobManager;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenyf on 2017/8/26.
 */
@Component
public class QuartzBiz {
    @Autowired
    ScheduleJobDao scheduleJobDao;
    @Autowired
    JobManager jobManager;
    @Autowired
    Producer producer;

    /**
     * 添加一个定时任务
     * @param scheduleJob
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResult<Date> add(ScheduleJob scheduleJob){
        String checkStr = this.checkAndInitJob(scheduleJob);
        if(StringUtil.isNotEmpty(checkStr)){
            return ServiceResult.fail(checkStr);
        }else if(getJobByName(scheduleJob.getJobGroup(), scheduleJob.getJobName()) != null){
            return ServiceResult.fail("jobGroup="+scheduleJob.getJobGroup()+",jobName="+scheduleJob.getJobName()+" 任务已存在");
        }

        int count = scheduleJobDao.insert(scheduleJob);//先保存到数据库，另其先返回ID
        if(count <= 0){
            return ServiceResult.fail("添加任务失败,保存入库失败");
        }

        ServiceResult<Date> resultBean = jobManager.addJob(scheduleJob);
        if(resultBean.isError()){
            //抛出异常，让整个事务回滚
            throw new BizException(resultBean.getMessage());
        }
        //还需要再次更新ScheduleJob，因为addJob方法内部会设置scheduleJob的jobStatus、nextExecuteTime等
        scheduleJobDao.updateIfNotNull(scheduleJob);
        return resultBean;
    }

    /**
     * 重新安排定时任务的定时规则
     * @param scheduleJob
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResult rescheduleJob(ScheduleJob scheduleJob){
        if(StringUtil.isEmpty(scheduleJob.getJobGroup())){
            return ServiceResult.fail("jobGroup不能为空");
        }else if(StringUtil.isEmpty(scheduleJob.getJobName())){
            return ServiceResult.fail("jobName不能为空");
        }

        ScheduleJob scheduleJobOld = getJobByName(scheduleJob.getJobGroup(), scheduleJob.getJobName());
        if(scheduleJobOld == null){
            return ServiceResult.fail("任务不存在");
        }

        //设置允许更新的属性值
        if(scheduleJobOld.getJobType().intValue() == ScheduleJob.SIMPLE_JOB){
            if(StringUtil.isNotEmpty(scheduleJob.getIntervals())){
                scheduleJobOld.setIntervals(scheduleJob.getIntervals());
            }
            if(StringUtil.isNotEmpty(scheduleJob.getIntervalUnit())){
                scheduleJobOld.setIntervalUnit(scheduleJob.getIntervalUnit());
            }
            if(StringUtil.isNotEmpty(scheduleJob.getRepeatTimes())){
                scheduleJobOld.setRepeatTimes(scheduleJob.getRepeatTimes());
            }
        }else if(scheduleJobOld.getJobType().intValue() == ScheduleJob.CRON_JOB){
            if(StringUtil.isNotEmpty(scheduleJob.getCronExpression())){
                scheduleJobOld.setCronExpression(scheduleJob.getCronExpression());
            }
        }
        if(StringUtil.isNotEmpty(scheduleJob.getTopic())){
            scheduleJobOld.setTopic(scheduleJob.getTopic());
        }
        if(StringUtil.isNotEmpty(scheduleJob.getTags())){
            scheduleJobOld.setTags(scheduleJob.getTags());
        }
        if(StringUtil.isNotEmpty(scheduleJob.getEndTime())){
            scheduleJobOld.setEndTime(scheduleJob.getEndTime());
        }
        if(StringUtil.isNotEmpty(scheduleJob.getParamJson())){
            scheduleJobOld.setParamJson(scheduleJob.getParamJson());
        }
        if(StringUtil.isNotEmpty(scheduleJob.getJobDescription())){
            scheduleJobOld.setJobDescription(scheduleJob.getJobDescription());
        }

        //执行更新
        ServiceResult resultBean = jobManager.rescheduleJob(scheduleJobOld);
        if(resultBean.isSuccess()){
            scheduleJobDao.update(scheduleJobOld);
        }
        return resultBean;
    }

    /**
     * 根据 组名+任务名 暂停定时任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResult pauseJob(String jobGroup, String jobName){
        ScheduleJob scheduleJob = getJobByName(jobGroup, jobName);
        if(scheduleJob == null){
            return ServiceResult.fail("任务不存在");
        }
        ServiceResult resultBean = jobManager.pauseJob(jobGroup, jobName);
        if(resultBean.isSuccess()){
            scheduleJob.setJobStatus(Trigger.TriggerState.PAUSED.name());
            scheduleJobDao.update(scheduleJob);
        }
        return resultBean;
    }

    /**
     * 根据 组名+任务名 恢复被暂停的定时任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResult resumeJob(String jobGroup, String jobName){
        ScheduleJob scheduleJob = getJobByName(jobGroup, jobName);
        if(scheduleJob == null){
            return ServiceResult.fail("任务不存在");
        }

        ServiceResult<String> resultBean = jobManager.resumeJob(jobGroup, jobName);
        if(resultBean.isSuccess()){
            scheduleJob.setJobStatus(resultBean.getData());
            scheduleJobDao.update(scheduleJob);
        }
        return resultBean;
    }

    /**
     * 根据 组名+任务名 立即执行一次定时任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResult triggerJob(String jobGroup, String jobName){
        ScheduleJob scheduleJob = getJobByName(jobGroup, jobName);
        if(scheduleJob == null){
            return ServiceResult.fail("任务不存在");
        }

        return jobManager.triggerJob(jobGroup, jobName);
    }

    /**
     * 根据 组名+任务名 删除定时任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResult delete(String jobGroup, String jobName){
        ScheduleJob scheduleJob = getJobByName(jobGroup, jobName);
        if(scheduleJob != null){
            scheduleJobDao.deleteByPk(scheduleJob.getId());
        }
        return jobManager.deleteJob(jobGroup, jobName);
    }

    /**
     * 根据 组名+任务名 取得定时任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    public ScheduleJob getJobByName(String jobGroup, String jobName){
        if(StringUtil.isEmpty(jobGroup)){
            throw new BizException("jobGroup不能为空");
        }else if(StringUtil.isEmpty(jobName)){
            throw new BizException("jobName不能为空");
        }
        return scheduleJobDao.getByName(jobGroup, jobName);
    }

    /**
     * 通知定时任务已触发(通过消息的形式通知)
     * @param scheduleJob
     * @return
     */
    public boolean notifyExecuteScheduleJob(ScheduleJob scheduleJob){
        PMessage msg = new PMessage();
        msg.setTopic(scheduleJob.getTopic());
        msg.setTags(scheduleJob.getTags());
        msg.setKey(scheduleJob.getJobGroup()+":"+scheduleJob.getJobName());
        msg.setMsgEvent(Integer.valueOf(scheduleJob.getMsgEvent()));
        msg.setBody(scheduleJob.getParamJson() == null ? "" : scheduleJob.getParamJson());
        Boolean isSuccess = producer.send(msg);
        return isSuccess;
    }

    /**
     * job被触发并执行完毕之后调用的方法，主要用来同步ScheduleJob一些属性，如：jobStatus、lastExecuteTime、nextExecuteTime、executedTimes等等
     * @param jobGroup
     * @param jobName
     * @param jobProperties
     * @return
     */
    public boolean updateScheduleJobAfterExecuted(String jobGroup, String jobName, Map<String, Object> jobProperties){
        if(jobProperties == null){
            jobProperties = new HashMap<>(2);
        }
        jobProperties.put("jobGroup", jobGroup);
        jobProperties.put("jobName", jobName);
        return scheduleJobDao.update("updateScheduleJobAfterExecuted", jobProperties) > 0;
    }

    /**
     * job在被检测到misfire之后调用的方法，主要用来同步ScheduleJob一些属性，如：jobStatus、lastExecuteTime、nextExecuteTime等等
     * @param jobGroup
     * @param jobName
     * @param jobProperties
     * @return
     */
    public boolean updateScheduleJobAfterMisfire(String jobGroup, String jobName, Map<String, Object> jobProperties){
        if(jobProperties == null){
            jobProperties = new HashMap<>(2);
        }
        jobProperties.put("jobGroup", jobGroup);
        jobProperties.put("jobName", jobName);
        return scheduleJobDao.update("updateScheduleJobAfterMisfire", jobProperties) > 0;
    }

    /**
     * 分页查询SchduleJob
     * @param paramMap
     * @param pageParam
     * @return
     */
    public PageResult<List<ScheduleJob>> listPage(Map<String, Object> paramMap, PageParam pageParam){
        return scheduleJobDao.listPage(paramMap, pageParam);
    }

    /**
     * 检查并初始化ScheduleJob
     * @param scheduleJob
     * @return
     */
    private String checkAndInitJob(ScheduleJob scheduleJob){
        //step1 参数校验
        if(scheduleJob == null){
            return "scheduleJob不能为空";
        }else if(scheduleJob.getJobType() == null){
            return "任务类型(jobType)不能为空";
        }else if(StringUtil.isEmpty(scheduleJob.getJobGroup())){
            return "任务的组名(jobGroup)不能为空";
        }else if(StringUtil.isEmpty(scheduleJob.getJobName())){
            return "任务名(jobName)不能为空";
        }else if(StringUtil.isEmpty(scheduleJob.getTopic())){
            return "任务通知的topic不能为空";
        }else if(StringUtil.isEmpty(scheduleJob.getTags())){
            return "任务通知的tags不能为空";
        }else if(StringUtil.isEmpty(scheduleJob.getMsgEvent())){
            return "任务通知的msgEvent不能为空";
        }else if(scheduleJob.getStartTime() == null){
            return "开始时间(startTime)不能为空";
        }
        if(scheduleJob.getJobType().equals(ScheduleJob.SIMPLE_JOB)){
            if(scheduleJob.getIntervals() == null){
                return "任务间隔(interval)不能为空";
            }else if(scheduleJob.getIntervalUnit() == null){
                return "任务间隔单位(intervalUnit)不能为空";
            }
        }else if(scheduleJob.getJobType().equals(ScheduleJob.CRON_JOB)){
            if(StringUtil.isEmpty(scheduleJob.getCronExpression())){
                return "cron表达式(cronExpression)不能为空";
            }
        }

        //step2 初始化
        if(scheduleJob.getExecutedTimes() == null){
            scheduleJob.setExecutedTimes(0L);
        }
        if(StringUtil.isEmpty(scheduleJob.getTags())){
            scheduleJob.setTags("*");
        }
        return "";
    }
}
