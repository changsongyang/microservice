package org.study.timer.provider.facade;

import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.study.common.statics.pojos.PageParam;
import org.study.common.statics.pojos.PageResult;
import org.study.common.statics.pojos.ServiceResult;
import org.study.timer.provider.core.biz.QuartzBiz;
import org.study.timer.api.entity.ScheduleJob;
import org.study.timer.api.service.QuartzService;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author chenyf on 2017/8/20.
 */
@Service
public class QuartzServiceImpl implements QuartzService {
    @Autowired
    QuartzBiz quartzBiz;

    /**
     * 直接通知，当实例处于暂停中的时候使用
     * @param jobGroup
     * @param jobName
     * @return
     */
    public ServiceResult sendJobNotify(String jobGroup, String jobName){
        return quartzBiz.sendJobNotify(jobGroup, jobName);
    }

    @Override
    public ServiceResult<Date> add(ScheduleJob scheduleJob){
        return quartzBiz.add(scheduleJob);
    }

    @Override
    public ServiceResult rescheduleJob(ScheduleJob scheduleJob){
        return quartzBiz.rescheduleJob(scheduleJob);
    }

    @Override
    public ServiceResult delete(String jobGroup, String jobName){
        return quartzBiz.delete(jobGroup, jobName);
    }

    @Override
    public ServiceResult pauseJob(String jobGroup, String jobName){
        return quartzBiz.pauseJob(jobGroup, jobName);
    }

    @Override
    public ServiceResult resumeJob(String jobGroup, String jobName){
        return quartzBiz.resumeJob(jobGroup, jobName);
    }

    @Override
    public ServiceResult triggerJob(String jobGroup, String jobName){
        return quartzBiz.triggerJob(jobGroup, jobName);
    }

    @Override
    public ScheduleJob getJobByName(String jobGroup, String jobName){
        return quartzBiz.getJobByName(jobGroup, jobName);
    }

    @Override
    public PageResult<List<ScheduleJob>> listPage(Map<String, Object> paramMap, PageParam pageParam){
        return quartzBiz.listPage(paramMap, pageParam);
    }


}
