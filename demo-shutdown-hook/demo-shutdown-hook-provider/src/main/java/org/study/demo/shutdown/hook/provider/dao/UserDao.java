package org.study.demo.shutdown.hook.provider.dao;

import org.springframework.stereotype.Repository;
import org.study.common.service.dao.MyBatisDao;

@Repository
public class UserDao extends MyBatisDao<User, Long> {

    public boolean reset(Long id){
        return update("reset", id) > 0;
    }
}
