package org.study.timer.provider.service;

import org.study.common.statics.pojos.PageParam;
import org.study.common.statics.pojos.PageResult;
import org.study.common.statics.pojos.ServiceResult;
import org.study.timer.provider.entity.ScheduleJob;

import java.util.List;
import java.util.Map;

/**
 * @author chenyf on 2017/8/20.
 */
public interface QuartzService {
    /**
     * 添加任务
     * @param scheduleJob
     * @return
     */
    public ServiceResult add(ScheduleJob scheduleJob);

    /**
     * 重新安排定时任务，即update任务
     * @param scheduleJob
     * @return
     */
    public ServiceResult rescheduleJob(ScheduleJob scheduleJob);

    /**
     * 删除任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    public ServiceResult delete(String jobGroup, String jobName);

    /**
     * 暂停任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    public ServiceResult pauseJob(String jobGroup, String jobName);

    /**
     * 恢复被暂停的任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    public ServiceResult resumeJob(String jobGroup, String jobName);

    /**
     * 立即触发任务
     * @param jobGroup
     * @param jobName
     * @return
     */
    public ServiceResult triggerJob(String jobGroup, String jobName);

    public ScheduleJob getJobByName(String jobGroup, String jobName);

    public PageResult<List<ScheduleJob>> listPage(Map<String, Object> paramMap, PageParam pageParam);
}
