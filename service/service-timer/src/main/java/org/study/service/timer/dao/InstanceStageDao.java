package org.study.service.timer.dao;

import org.springframework.stereotype.Repository;
import org.study.common.service.dao.MyBatisDao;
import org.study.common.statics.exceptions.BizException;
import org.study.common.util.utils.StringUtil;
import org.study.facade.timer.entity.InstanceStage;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chenyf on 2017/8/29.
 */
@Repository
public class InstanceStageDao extends MyBatisDao<InstanceStage, Long> {

    public InstanceStage getByInstanceId(String instanceId){
        if(StringUtil.isEmpty(instanceId)){
            throw new BizException("instanceId不能为空");
        }
        Map<String, String> param = new HashMap<String, String>();
        param.put("instanceId", instanceId);
        return super.getOne(param);
    }
}
