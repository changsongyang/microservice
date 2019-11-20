package org.study.service.timer.dao;

import org.springframework.stereotype.Repository;
import org.study.common.service.dao.MyBatisDao;
import org.study.facade.timer.entity.ScheduleJob;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chenyf on 2017/8/29.
 */
@Repository
public class ScheduleJobDao extends MyBatisDao<ScheduleJob, Long> {

    public ScheduleJob getByName(String jobGroup, String jobName){
        Map<String, String> param = new HashMap<String, String>();
        param.put("jobGroup", jobGroup);
        param.put("jobName", jobName);
        return super.getOne(param);
    }
}
