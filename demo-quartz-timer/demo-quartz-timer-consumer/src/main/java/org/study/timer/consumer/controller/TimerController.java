package org.study.timer.consumer.controller;

import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.study.common.statics.constants.MsgTopicAndTags;
import org.study.common.statics.enums.TimeUnitEnum;
import org.study.common.statics.pojos.PageParam;
import org.study.common.statics.pojos.PageResult;
import org.study.common.statics.pojos.RestResult;
import org.study.common.statics.pojos.ServiceResult;
import org.study.common.util.utils.JsonUtil;
import org.study.timer.api.entity.ScheduleJob;
import org.study.timer.api.service.QuartzAdminService;
import org.study.timer.api.service.QuartzService;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("timer")
public class TimerController {
    @Reference
    QuartzService quartzService;
    @Reference
    QuartzAdminService quartzAdminService;

    @RequestMapping(value = "addSimpleTimer", method = RequestMethod.POST)
    public RestResult addSimpleTimer(Integer intervalSecond, String jobDescription, String param){
        if(intervalSecond == null || intervalSecond <= 0){
            return RestResult.bizSuccess(101, "intervalSecond必须大于0");
        }
        String jobGroup = "simpleTimerGroup";
        String jobName = "simpleTimerJob";
        ScheduleJob scheduleJob = ScheduleJob.newSimpleTask(
                jobGroup, jobName, MsgTopicAndTags.TOPIC_USER_BIZ, MsgTopicAndTags.TAG_TIMER_SIMPLE);
        scheduleJob.setStartTime(new Date());
        scheduleJob.setIntervals(intervalSecond);
        scheduleJob.setIntervalUnit(TimeUnitEnum.SECOND.getValue());
        scheduleJob.setJobDescription(jobDescription);

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("param", param);
        scheduleJob.setParamJson(JsonUtil.toString(paramMap));

        ServiceResult serviceResult = quartzService.add(scheduleJob);
        if(serviceResult.isSuccess()){
            return RestResult.bizSuccess(100, "添加简单定时任务成功");
        }else{
            return RestResult.bizFail(101, serviceResult.getMessage());
        }
    }

    @RequestMapping(value = "addCronTimer", method = RequestMethod.POST)
    public RestResult addCronTimer(String cron, String jobDescription, String param){
        if(cron == null || cron.trim().length() <= 0){
            return RestResult.bizSuccess(101, "cron表达式不能为空");
        }
        String jobGroup = "cronTimerGroup";
        String jobName = "cronTimerJob";
        ScheduleJob scheduleJob = ScheduleJob.newCronTask(
                jobGroup, jobName, MsgTopicAndTags.TOPIC_USER_BIZ, MsgTopicAndTags.TAG_TIMER_CRON);
        scheduleJob.setStartTime(new Date());
        scheduleJob.setCronExpression(cron);
        scheduleJob.setJobDescription(jobDescription);

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("param", param);
        scheduleJob.setParamJson(JsonUtil.toString(paramMap));

        ServiceResult serviceResult = quartzService.add(scheduleJob);
        if(serviceResult.isSuccess()){
            return RestResult.bizSuccess(100, "添加cron定时任务成功");
        }else{
            return RestResult.bizFail(101, serviceResult.getMessage());
        }
    }

    @RequestMapping(value = "listTimer", method = RequestMethod.POST)
    public RestResult listTimer(){
        PageResult<List<ScheduleJob>> pageResult = quartzService.listPage(new HashMap<>(), PageParam.newInstance(1, 1000));
        if(pageResult.getData() != null){
            return RestResult.bizSuccess(100, pageResult.getData());
        }else{
            return RestResult.bizSuccess(101, "获取结果为空");
        }
    }

    @RequestMapping(value = "deleteTimer", method = RequestMethod.POST)
    public RestResult deleteTimer(String jobGroup, String jobName){
        ServiceResult serviceResult = quartzService.delete(jobGroup, jobName);
        if(serviceResult.isSuccess()){
            return RestResult.bizSuccess(100, "删除成功");
        }else{
            return RestResult.bizFail(101, serviceResult.getMessage());
        }
    }

    @RequestMapping(value = "pauseTimer", method = RequestMethod.POST)
    public RestResult pauseTimer(String jobGroup, String jobName){
        ServiceResult serviceResult = quartzService.pauseJob(jobGroup, jobName);
        if(serviceResult.isSuccess()){
            return RestResult.bizSuccess(100, "暂停成功");
        }else{
            return RestResult.bizFail(101, serviceResult.getMessage());
        }
    }

    @RequestMapping(value = "resumeJob", method = RequestMethod.POST)
    public RestResult resumeJob(String jobGroup, String jobName){
        ServiceResult serviceResult = quartzService.resumeJob(jobGroup, jobName);
        if(serviceResult.isSuccess()){
            return RestResult.bizSuccess(100, "恢复成功");
        }else{
            return RestResult.bizFail(101, serviceResult.getMessage());
        }
    }

    @RequestMapping(value = "pauseInstance", method = RequestMethod.POST)
    public RestResult pauseInstance(){
        quartzAdminService.pauseInstance();
        return RestResult.bizSuccess(100, "实例暂停成功");
    }

    @RequestMapping(value = "resumeInstance", method = RequestMethod.POST)
    public RestResult resumeInstance(){
        quartzAdminService.resumeInstance();
        return RestResult.bizSuccess(100, "实例恢复成功");
    }
}
