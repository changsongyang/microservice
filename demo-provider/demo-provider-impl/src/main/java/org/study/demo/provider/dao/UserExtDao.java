package org.study.demo.provider.dao;

import org.springframework.stereotype.Repository;
import org.study.common.service.dao.MyBatisDao;
import org.study.demo.provider.entity.UserExt;

@Repository
public class UserExtDao extends MyBatisDao<UserExt, String> {

}
