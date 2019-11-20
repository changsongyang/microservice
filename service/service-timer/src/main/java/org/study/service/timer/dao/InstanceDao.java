package com.xpay.service.timer.dao;

import com.xpay.common.statics.exceptions.BizException;
import com.xpay.common.util.utils.StringUtil;
import com.xpay.facade.timer.entity.Instance;
import org.springframework.stereotype.Repository;
import com.xpay.common.service.dao.MyBatisDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenyf on 2017/8/29.
 */
@Repository
public class InstanceDao extends MyBatisDao<Instance, Long> {

    public Instance getByInstanceId(String instanceId){
        if(StringUtil.isEmpty(instanceId)){
            throw new BizException("instanceId不能为空");
        }
        Map<String, String> param = new HashMap<String, String>();
        param.put("instanceId", instanceId);
        return super.getOne(param);
    }

    public List<Instance> listByNamespace(String namespace){
        if(StringUtil.isEmpty(namespace)){
            throw new BizException("namespace不能为空");
        }
        Map<String, Object> param = new HashMap<>();
        param.put("namespace", namespace);
        return listBy(param);
    }

    public boolean updateInstanceStatus(Integer stage, String instanceId, String namespace){
        Map<String, Object> param = new HashMap<>();
        param.put("status", stage);
        param.put("instanceId", instanceId);
        param.put("namespace", namespace);
        return update("updateInstanceStatus", param) > 0;
    }
}
