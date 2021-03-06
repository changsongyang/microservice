package org.study.facade.timer.service;

import org.study.common.statics.exceptions.BizException;
import org.study.common.statics.pojos.PageParam;
import org.study.common.statics.pojos.PageResult;
import org.study.facade.timer.entity.ScheduleJob;

import java.util.List;
import java.util.Map;

/**
 * @author chenyf on 2017/8/20.
 */
public interface QuartzService {

    /**
     * 直接触发任务的消息通知，主要在实例处于挂起状态，但又需要对个别任务进行测试时使用
     * @param jobGroup
     * @param jobName
     * @return
     */
    public boolean sendJobNotify(String jobGroup, String jobName);

    /**
     * 添加任务
     * @param scheduleJob
     * @return
     */
    public Long add(ScheduleJob scheduleJob) throws BizException;

    /**
     * 重新安排定时任务，即update任务
     * @param scheduleJob
     * @return
     */
    public boolean rescheduleJob(ScheduleJob scheduleJob) throws BizException;

    /**
     * 删除任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    public boolean delete(String jobGroup, String jobName) throws BizException;

    /**
     * 暂停任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    public boolean pauseJob(String jobGroup, String jobName) throws BizException;

    /**
     * 恢复被暂停的任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    public boolean resumeJob(String jobGroup, String jobName) throws BizException;

    /**
     * 立即触发任务，若实例处于挂起状态，则操作无效
     * @param jobGroup
     * @param jobName
     * @return
     */
    public boolean triggerJob(String jobGroup, String jobName) throws BizException;

    public ScheduleJob getJobByName(String jobGroup, String jobName);

    public PageResult<List<ScheduleJob>> listPage(Map<String, Object> paramMap, PageParam pageParam);
}
