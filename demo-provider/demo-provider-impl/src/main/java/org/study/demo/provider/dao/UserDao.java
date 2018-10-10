package org.study.demo.provider.dao;

import org.springframework.stereotype.Repository;
import org.study.common.service.dao.MyBatisDao;
import org.study.demo.provider.entity.User;

@Repository
public class UserDao extends MyBatisDao<User, Long> {

}
