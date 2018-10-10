package org.study.demo.restful.web.controllers;

import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import org.study.common.statics.pojos.RestResult;
import org.study.demo.provider.api.UserService;
import org.study.demo.provider.entity.User;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("dubbo")
public class DubboController {
    @Reference
    UserService userService;

    @RequestMapping("getUserById")
    public RestResult<User> getUserById(Long id){
        User user = userService.getById(id);
        return RestResult.bizSuccess(100, user);
    }

    @RequestMapping("getUserByName")
    public RestResult<User> getUserByName(String userName){
        User user = userService.getByUserName(userName);
        return RestResult.bizSuccess(100, user);
    }

    @RequestMapping("listUser")
    public RestResult<List<User>> listUser(String userName, Long id, Integer gender){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userName", userName);
        paramMap.put("id", id);
        paramMap.put("gender", gender);
        List<User> userList = userService.listBy(paramMap);
        return RestResult.bizSuccess(100, userList);
    }

    @RequestMapping(value = "addUser", method = RequestMethod.POST)
    public RestResult addUser(@RequestBody User user){
        long id = userService.addUser(user);
        if(id > 0){
            return RestResult.bizSuccess(100, id);
        }else{
            return RestResult.bizFail(101, "添加用户成功");
        }
    }

    @RequestMapping(value = "addUserAndNotify", method = RequestMethod.POST)
    public RestResult addUserAndNotify(@RequestBody User user){
        long id = userService.addUserAndNotify(user);
        if(id > 0){
            return RestResult.bizSuccess(100, id);
        }else{
            return RestResult.bizFail(101, "用户注册失败");
        }
    }

    @RequestMapping(value = "updateUserExt", method = RequestMethod.POST)
    public RestResult updateUserExt(long id, Integer age, String address, Integer height, BigDecimal weight){
        boolean isSuccess = userService.updateUserExt(id, age, address, height, weight);
        if(isSuccess){
            return RestResult.bizSuccess(100, id);
        }else{
            return RestResult.bizFail(101, "用户信息更新成功");
        }
    }
}
