package org.study.demo.provider.service;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.study.demo.provider.api.UserService;
import org.study.demo.provider.biz.UserBiz;
import org.study.demo.provider.entity.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class UserSerivceImpl implements UserService {
    @Autowired
    UserBiz userBiz;

    public User getById(Long id){
        return userBiz.getById(id);
    }

    public User getByUserName(String userName){
        return userBiz.getByUserName(userName);
    }

    public List<User> listBy(Map<String, Object> paramMap){
        return userBiz.listBy(paramMap);
    }

    public long addUser(User user){
        return userBiz.addUser(user);
    }

    public long addUserAndNotify(User user){
        return userBiz.addUserAndNotify(user);
    }

    public boolean updateUserExt(long id, Integer age, String address, Integer height, BigDecimal weight){
        return userBiz.sendUpdateUserExt(id, age, address, height, weight);
    }
}
