package org.study.timer.provider.core.biz;

import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.study.common.statics.exceptions.BizException;
import org.study.common.statics.pojos.PageParam;
import org.study.common.statics.pojos.PageResult;
import org.study.common.statics.pojos.ServiceResult;
import org.study.common.statics.vo.MessageVo;
import org.study.common.util.utils.DateUtil;
import org.study.common.util.utils.StringUtil;
import org.study.starter.component.RocketMQSender;
import org.study.timer.provider.core.dao.ScheduleJobDao;
import org.study.timer.provider.core.job.base.JobManager;
import org.study.timer.api.entity.ScheduleJob;

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
    RocketMQSender rocketMQSender;
    @Autowired
    JobManager jobManager;

    public ServiceResult sendJobNotify(String jobGroup, String jobName){
        ScheduleJob scheduleJob = scheduleJobDao.getByName(jobGroup, jobName);
        if(scheduleJob == null){
            return ServiceResult.fail("任务不存在");
        }

        boolean isOk = notifyExecuteScheduleJob(scheduleJob);
        if(isOk){
            return ServiceResult.success("通知成功");
        }else{
            return ServiceResult.fail("通知失败");
        }
    }

    /**
     * 添加一个定时任务
     * @param scheduleJob
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResult<Date> add(ScheduleJob scheduleJob){
        try{
            this.initScheduleJob(scheduleJob);
            scheduleJobDao.insert(scheduleJob);
            ServiceResult<Date> resultBean = jobManager.addJob(scheduleJob);
            //还需要再次更新ScheduleJob，因为addJob方法内部会设置scheduleJob的jobStatus、nextExecuteTime等
            scheduleJobDao.updateIfNotNull(scheduleJob);
            return resultBean;
        }catch (Throwable e){
            throw new BizException("添加任务发生异常", e);
        }
    }

    /**
     * 重新安排定时任务的定时规则
     * @param scheduleJob
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResult rescheduleJob(ScheduleJob scheduleJob){
        try{
            ScheduleJob scheduleJobTemp = scheduleJobDao.getByName(scheduleJob.getJobGroup(), scheduleJob.getJobName());
            if(scheduleJobTemp == null){
                return ServiceResult.fail("任务不存在");
            }else if(scheduleJobTemp.getJobType().intValue() != ScheduleJob.SIMPLE_JOB
                    && scheduleJobTemp.getJobType().intValue() != ScheduleJob.CRON_JOB){
                return ServiceResult.fail("未预期的任务类型");
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
            if(StringUtil.isNotEmpty(scheduleJob.getTopic())){
                scheduleJobTemp.setTopic(scheduleJob.getTopic());
            }
            if(StringUtil.isNotEmpty(scheduleJob.getTags())){
                scheduleJobTemp.setTags(scheduleJob.getTags());
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

            //执行更新
            ServiceResult resultBean = jobManager.rescheduleJob(scheduleJobTemp);
            if(resultBean.isSuccess()){
                scheduleJobDao.update(scheduleJobTemp);
            }
            return resultBean;
        }catch (Throwable e){
            throw new BizException("重新安排任务发生异常", e);
        }
    }

    /**
     * 根据 组名+任务名 暂停定时任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResult pauseJob(String jobGroup, String jobName){
        try{
            ScheduleJob scheduleJob = scheduleJobDao.getByName(jobGroup, jobName);
            if(scheduleJob == null){
                return ServiceResult.fail("任务不存在");
            }
            ServiceResult resultBean = jobManager.pauseJob(jobGroup, jobName);
            if(resultBean.isSuccess()){
                scheduleJob.setJobStatus(Trigger.TriggerState.PAUSED.name());
                scheduleJobDao.update(scheduleJob);
            }
            return resultBean;
        }catch (Throwable e){
            throw new BizException("暂停任务发生异常", e);
        }
    }

    /**
     * 根据 组名+任务名 恢复被暂停的定时任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResult resumeJob(String jobGroup, String jobName){
        try{
            ScheduleJob scheduleJob = scheduleJobDao.getByName(jobGroup, jobName);
            if(scheduleJob == null){
                return ServiceResult.fail("任务不存在");
            }

            ServiceResult<String> resultBean = jobManager.resumeJob(jobGroup, jobName);
            if(resultBean.isSuccess()){
                scheduleJob.setJobStatus(resultBean.getData());
                scheduleJobDao.update(scheduleJob);
            }
            return resultBean;
        }catch (Throwable e){
            throw new BizException("恢复任务发生异常", e);
        }
    }

    /**
     * 根据 组名+任务名 立即执行一次定时任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResult triggerJob(String jobGroup, String jobName){
        try{
            ScheduleJob scheduleJob = scheduleJobDao.getByName(jobGroup, jobName);
            if(scheduleJob == null){
                return ServiceResult.fail("任务不存在");
            }

            return jobManager.triggerJob(jobGroup, jobName);
        }catch (Throwable e){
            throw new BizException("触发任务发生异常", e);
        }
    }

    /**
     * 根据 组名+任务名 删除定时任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResult delete(String jobGroup, String jobName){
        try{
            ScheduleJob scheduleJob = scheduleJobDao.getByName(jobGroup, jobName);
            if(scheduleJob != null){
                scheduleJobDao.deleteByPk(scheduleJob.getId());
            }
            jobManager.deleteJob(jobGroup, jobName);
            return ServiceResult.success();
        }catch (Throwable e){
            throw new BizException("删除任务发生异常", e);
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
     * 通知定时任务已触发(通过消息的形式通知)
     * @param scheduleJob
     * @return
     */
    public boolean notifyExecuteScheduleJob(ScheduleJob scheduleJob){
        MessageVo msg = new MessageVo();
        msg.setTopic(scheduleJob.getTopic());
        msg.setTags(scheduleJob.getTags());
        msg.setMsgType(0);
        msg.setTrxNo(scheduleJob.getJobGroup() + "_" + scheduleJob.getJobName() + "_" + DateUtil.formatDateTime(new Date()));
        msg.setJsonParam(scheduleJob.getParamJson()==null?"":scheduleJob.getParamJson());
        //发送消息通知
        rocketMQSender.sendOne(msg);
        return true;
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
}
