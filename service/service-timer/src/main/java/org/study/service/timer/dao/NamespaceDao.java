package com.xpay.service.timer.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.facade.timer.entity.Namespace;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class NamespaceDao extends MyBatisDao<Namespace, String> {

    public boolean updateStatus(Integer status, String namespace){
        Map<String, Object> param = new HashMap<>();
        param.put("status", status);
        param.put("namespace", namespace);
        return update("updateStatus", param) > 0;
    }
}
