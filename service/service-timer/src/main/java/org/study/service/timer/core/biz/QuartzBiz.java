package org.study.service.timer.core.biz;

import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.study.common.statics.exceptions.BizException;
import org.study.common.statics.pojos.PageParam;
import org.study.common.statics.pojos.PageResult;
import org.study.common.util.utils.JsonUtil;
import org.study.common.util.utils.StringUtil;
import org.study.service.timer.core.dao.ScheduleJobDao;
import org.study.service.timer.core.job.base.JobManager;
import org.study.service.timer.core.job.base.JobNotifier;
import org.study.facade.timer.entity.ScheduleJob;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenyf on 2017/8/26.
 */
@Component
public class QuartzBiz {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    ScheduleJobDao scheduleJobDao;
    @Autowired
    JobNotifier mqSender;
    @Autowired
    JobManager jobManager;

    public boolean sendJobNotify(String jobGroup, String jobName){
        ScheduleJob scheduleJob = scheduleJobDao.getByName(jobGroup, jobName);
        if(scheduleJob == null){
            throw new BizException(BizException.BIZ_VALIDATE_ERROR, "任务不存在");
        }

        return mqSender.sendScheduleMessage(scheduleJob);
    }

    /**
     * 添加一个定时任务
     * @param scheduleJob
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Long add(ScheduleJob scheduleJob){
        this.checkJobParam(scheduleJob);
        this.initScheduleJob(scheduleJob);

        try{
            scheduleJobDao.insert(scheduleJob);
            Date startTime = jobManager.addJob(scheduleJob);
            //还需要再次更新ScheduleJob，因为addJob方法内部会设置scheduleJob的jobStatus、nextExecuteTime等
            if(startTime != null){
                scheduleJobDao.updateIfNotNull(scheduleJob);
                return scheduleJob.getId();
            }else{
                //抛出异常让事务回滚
                throw new BizException("添加任务失败");
            }
        }catch (BizException e){
            throw e;
        }catch (Throwable e){
            logger.error("添加任务时出现异常 ScheduleJob = {} ", JsonUtil.toString(scheduleJob), e);
            throw new BizException("添加任务发生异常", e);
        }
    }

    /**
     * 重新安排定时任务的定时规则
     * @param scheduleJob
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean rescheduleJob(ScheduleJob scheduleJob){
        if(scheduleJob == null){
            throw new BizException("scheduleJob为空");
        }else if(StringUtil.isEmpty(scheduleJob.getJobGroup())){
            throw new BizException("jobGroup为空");
        }else if(StringUtil.isEmpty(scheduleJob.getJobName())){
            throw new BizException("jobName为空");
        }

        ScheduleJob scheduleJobTemp = scheduleJobDao.getByName(scheduleJob.getJobGroup(), scheduleJob.getJobName());
        if(scheduleJobTemp == null){
            throw new BizException("任务不存在");
        }

        //设置允许更新的属性值
        if(scheduleJobTemp.getJobType().intValue() == ScheduleJob.SIMPLE_JOB){
            if(StringUtil.isNotEmpty(scheduleJob.getIntervals())){
                scheduleJobTemp.setIntervals(scheduleJob.getIntervals());
            }
            if(StringUtil.isNotEmpty(scheduleJob.getIntervalUnit())){
                scheduleJobTemp.setIntervalUnit(scheduleJob.getIntervalUnit());
            }
            if(StringUtil.isNotEmpty(scheduleJob.getRepeatTimes())){
                scheduleJobTemp.setRepeatTimes(scheduleJob.getRepeatTimes());
            }
        }else if(scheduleJobTemp.getJobType().intValue() == ScheduleJob.CRON_JOB){
            if(StringUtil.isNotEmpty(scheduleJob.getCronExpression())){
                scheduleJobTemp.setCronExpression(scheduleJob.getCronExpression());
            }
        }
        if(StringUtil.isNotEmpty(scheduleJob.getDestination())){
            scheduleJobTemp.setDestination(scheduleJob.getDestination());
        }
        if(StringUtil.isNotEmpty(scheduleJob.getMqType())){
            scheduleJobTemp.setMqType(scheduleJob.getMqType());
        }
        if(StringUtil.isNotEmpty(scheduleJob.getEndTime())){
            scheduleJobTemp.setEndTime(scheduleJob.getEndTime());
        }
        if(StringUtil.isNotEmpty(scheduleJob.getParamJson())){
            scheduleJobTemp.setParamJson(scheduleJob.getParamJson());
        }
        if(StringUtil.isNotEmpty(scheduleJob.getJobDescription())){
            scheduleJobTemp.setJobDescription(scheduleJob.getJobDescription());
        }

        try{
            scheduleJobDao.update(scheduleJobTemp);
            //执行更新
            Date startTime = jobManager.rescheduleJob(scheduleJobTemp);
            if(startTime != null){
                return true;
            }else{
                //抛出异常让事务回滚
                throw new BizException("操作失败");
            }
        }catch (BizException e){
            throw e;
        }catch (Throwable e){
            logger.error("重新安排任务时出现异常 ScheduleJob = {} ", JsonUtil.toString(scheduleJob), e);
            throw new BizException("重新安排任务时发生异常", e);
        }
    }

    /**
     * 根据 组名+任务名 暂停定时任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean pauseJob(String jobGroup, String jobName){
        ScheduleJob scheduleJob = scheduleJobDao.getByName(jobGroup, jobName);
        if(scheduleJob == null){
            throw new BizException("任务不存在");
        }

        try{
            scheduleJob.setJobStatus(Trigger.TriggerState.PAUSED.name());
            scheduleJobDao.update(scheduleJob);
            jobManager.pauseJob(jobGroup, jobName);
            return true;
        }catch (BizException e){
            throw e;
        }catch (Throwable e){
            logger.error("暂停任务时出现异常 jobGroup={} jobName={} ", jobGroup, jobName, e);
            throw new BizException("暂停任务时发生异常", e);
        }
    }

    /**
     * 根据 组名+任务名 恢复被暂停的定时任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean resumeJob(String jobGroup, String jobName){
        ScheduleJob scheduleJob = scheduleJobDao.getByName(jobGroup, jobName);
        if(scheduleJob == null){
            throw new BizException("任务不存在");
        }

        try{
            String status = jobManager.resumeJob(jobGroup, jobName);
            if(StringUtil.isEmpty(status)){
                return false;
            }

            scheduleJob.setJobStatus(status);
            scheduleJobDao.update(scheduleJob);
            return true;
        }catch (BizException e){
            throw e;
        }catch (Throwable e){
            logger.error("恢复任务时出现异常 jobGroup={} jobName={} ", jobGroup, jobName, e);
            throw new BizException("恢复任务时发生异常", e);
        }
    }

    /**
     * 根据 组名+任务名 立即执行一次定时任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean triggerJob(String jobGroup, String jobName){
        ScheduleJob scheduleJob = scheduleJobDao.getByName(jobGroup, jobName);
        if(scheduleJob == null){
            throw new BizException("任务不存在");
        }

        try{
            jobManager.triggerJob(jobGroup, jobName);
            return true;
        }catch (BizException e){
            throw e;
        }catch (Throwable e){
            logger.error("触发任务时出现异常 jobGroup={} jobName={} ", jobGroup, jobName, e);
            throw new BizException("触发任务时发生异常", e);
        }
    }

    /**
     * 根据 组名+任务名 删除定时任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(String jobGroup, String jobName){
        try{
            ScheduleJob scheduleJob = scheduleJobDao.getByName(jobGroup, jobName);
            if(scheduleJob != null){
                scheduleJobDao.deleteByPk(scheduleJob.getId());
            }
            jobManager.deleteJob(jobGroup, jobName);
            return true;
        }catch (BizException e){
            throw e;
        }catch (Throwable e){
            logger.error("删除任务时出现异常 jobGroup={} jobName={} ", jobGroup, jobName, e);
            throw new BizException("删除任务时发生异常", e);
        }
    }

    /**
     * 根据 组名+任务名 取得定时任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    public ScheduleJob getJobByName(String jobGroup, String jobName){
        return scheduleJobDao.getByName(jobGroup, jobName);
    }

    /**
     * job被触发并执行完毕之后调用的方法，主要用来同步ScheduleJob一些属性，如：jobStatus、lastExecuteTime、nextExecuteTime、executedTimes等等
     * @param jobGroup
     * @param jobName
     * @param jobProperties
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateScheduleJobAfterExecuted(String jobGroup, String jobName, Map<String, Object> jobProperties){
        if(jobProperties == null){
            jobProperties = new HashMap<String, Object>(2);
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
    @Transactional(rollbackFor = Exception.class)
    public boolean updateScheduleJobAfterMisfire(String jobGroup, String jobName, Map<String, Object> jobProperties){
        if(jobProperties == null){
            jobProperties = new HashMap<String, Object>(2);
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
     * 初始化ScheduleJob，把一些必填的属性给赋上默认值
     * @param scheduleJob
     */
    private void initScheduleJob(ScheduleJob scheduleJob){
        if(scheduleJob.getExecutedTimes() == null){
            scheduleJob.setExecutedTimes(0L);
        }
    }

    private void checkJobParam(ScheduleJob scheduleJob){
        if(scheduleJob == null){
            throw new BizException(BizException.PARAM_VALIDATE_ERROR, "scheduleJob不能为空");
        }else if(scheduleJob.getJobType() == null){
            throw new BizException(BizException.PARAM_VALIDATE_ERROR, "任务类型(jobType)不能为空");
        }else if(StringUtil.isEmpty(scheduleJob.getJobGroup())){
            throw new BizException(BizException.PARAM_VALIDATE_ERROR, "任务的组名(jobGroup)不能为空");
        }else if(StringUtil.isEmpty(scheduleJob.getJobName())){
            throw new BizException(BizException.PARAM_VALIDATE_ERROR, "任务名(jobName)不能为空");
        }else if(StringUtil.isEmpty(scheduleJob.getDestination())){
            throw new BizException(BizException.PARAM_VALIDATE_ERROR, "任务通知目的地不能为空");
        }else if(StringUtil.isEmpty(scheduleJob.getMqType())){
            throw new BizException(BizException.PARAM_VALIDATE_ERROR, "消息中间件类型不能为空");
        }else if(scheduleJob.getStartTime() == null){
            throw new BizException(BizException.PARAM_VALIDATE_ERROR, "开始时间(startTime)不能为空");
        }

        if(scheduleJob.getJobType().equals(ScheduleJob.SIMPLE_JOB)){
            if(scheduleJob.getIntervals() == null){
                throw new BizException(BizException.PARAM_VALIDATE_ERROR, "任务间隔(interval)不能为空");
            }else if(scheduleJob.getIntervalUnit() == null){
                throw new BizException(BizException.PARAM_VALIDATE_ERROR, "任务间隔单位(intervalUnit)不能为空");
            }
        }else if(scheduleJob.getJobType().equals(ScheduleJob.CRON_JOB)){
            if(StringUtil.isEmpty(scheduleJob.getCronExpression())){
                throw new BizException(BizException.PARAM_VALIDATE_ERROR, "cron表达式(cronExpression)不能为空");
            }
        }else{
            throw new BizException(BizException.PARAM_VALIDATE_ERROR, "未支持的任务类型: " + scheduleJob.getJobType());
        }

        if(! scheduleJob.getMqType().equals(ScheduleJob.MQ_TYPE_ROCKET) && ! scheduleJob.getMqType().equals(ScheduleJob.MQ_TYPE_ACTIVE)){
            throw new BizException(BizException.PARAM_VALIDATE_ERROR, "未支持的MQ类型: " + scheduleJob.getMqType());
        }
    }
}
