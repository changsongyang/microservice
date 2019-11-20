package org.study.service.timer.dao;

import org.springframework.stereotype.Repository;
import org.study.common.service.dao.MyBatisDao;
import org.study.facade.timer.entity.Namespace;

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
