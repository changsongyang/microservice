package org.study.demo.provider.api;

import org.study.demo.provider.entity.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface UserService {
    User getById(Long id);

    User getByUserName(String userName);

    List<User> listBy(Map<String, Object> paramMap);

    long addUser(User user);

    long addUserAndNotify(User user);

    public boolean updateUserExt(long id, Integer age, String address, Integer height, BigDecimal weight);
}
