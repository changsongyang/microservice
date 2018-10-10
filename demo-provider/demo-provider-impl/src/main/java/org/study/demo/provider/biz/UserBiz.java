package org.study.demo.provider.biz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.study.common.mq.message.PMessage;
import org.study.common.mq.producer.Producer;
import org.study.common.statics.constants.MsgEvent;
import org.study.common.statics.constants.MsgTopicAndTags;
import org.study.common.statics.exceptions.BizException;
import org.study.common.util.utils.DateUtil;
import org.study.demo.provider.dao.UserDao;
import org.study.demo.provider.dao.UserExtDao;
import org.study.demo.provider.entity.User;
import org.study.demo.provider.entity.UserExt;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserBiz {
    @Autowired
    UserDao userDao;
    @Autowired
    UserExtDao userExtDao;
    @Autowired
    Producer producer;

    /**-----------------------------------用以测试Dubbo服务的增删改查 START---------------------------------*/
    public boolean deleteById(Long id){
        return userDao.deleteByPk(id) > 0;
    }
    public User getById(Long id){
        return userDao.getByPk(id);
    }
    public User getByUserName(String userName){
        Map<String, Object> param = new HashMap<>();
        param.put("userName", userName);
        return userDao.getOne(param);
    }
    public List<User> listBy(Map<String, Object> paramMap){
        return userDao.listBy(paramMap);
    }
    public long addUser(User user){
        user.setCreateTime(new Date());
        int insertCount = userDao.insert(user);
        if(insertCount > 0){
            return user.getId();
        }else{
            return 0;
        }
    }
    /**-----------------------------------用以测试Dubbo服务的增删改查 END---------------------------------*/




    /**-----------------------------------用以测试RocketMQ消息的发送和消费 START---------------------------------*/
    /**
     * 添加用户，然后发送通知，用以测试：1、是否可以正常发送消息；2、在同一个应用里面是否既可以正常发送消息又可正常消费消息
     * @param user
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public long addUserAndNotify(User user){
        user.setCreateTime(new Date());
        int insertCount = userDao.insert(user);

        long userId = 0;
        if(insertCount > 0){
            userId = user.getId();
        }

        //用户注册成功，发送MQ消息，给用户发送注册成功的邮件
        if(userId > 0){
            String key = "RG"+DateUtil.formatShortDate(new Date()) + String.format("%1$08d", user.getId());
            PMessage<User> msg = new PMessage<>();
            msg.setTopic(MsgTopicAndTags.TOPIC_USER_BIZ);
            msg.setTags(MsgTopicAndTags.TAG_USER_SEND_EMAIL);
            msg.setMsgEvent(MsgEvent.USER_REGISTER);
            msg.setKey(key);
            msg.setBody(user);
            producer.send(msg);
        }
        return userId;
    }
    /**
     * 发送更新userExt的通知，可用以测试：1、是否可以正常发送事务消息
     * @param id
     * @param age
     * @param address
     * @param height
     * @param weight
     * @return
     */
    public boolean sendUpdateUserExt(long id, Integer age, String address, Integer height, BigDecimal weight){
        String key = "UUE"+DateUtil.formatShortDate(new Date()) + String.format("%1$08d", id);

        Map<String, String> map = new HashMap<>();
        map.put("id", String.valueOf(id));
        map.put("age", String.valueOf(age));
        map.put("address", address);
        map.put("height", String.valueOf(height));
        map.put("weight", String.valueOf(weight));

        PMessage<Map<String, String>> msg = new PMessage<>();
        msg.setTopic(MsgTopicAndTags.TOPIC_USER_BIZ);
        msg.setTags(MsgTopicAndTags.TAG_USER_UPDATE_EXT);
        msg.setMsgEvent(MsgEvent.USER_UPDATE_EXT);
        msg.setKey(key);
        msg.setBody(map);
        producer.sendTransaction(msg);
        return true;
    }
    /**
     * 实际执行userExt的更新(即事务消息中的本地事务)，可用以测试：1、spring+mybatis+jdbc的事务是否正常回滚，2、RMQ消息回调时事务是否能正常回滚
     * @param id
     * @param age
     * @param address
     * @param height
     * @param weight
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean doUpdateUserExt(long id, Integer age, String address, Integer height, BigDecimal weight){
        User user = getById(id);
        if(user == null){
            return false;
        }

        UserExt userExt = userExtDao.getByPk(user.getUserNo());
        if(userExt == null){
            userExt = new UserExt();
            userExt.setUserNo(user.getUserNo());
            userExt.setAddress(address);
            userExt.setHeight(height==null?0:height);
            userExt.setWeight(weight==null?BigDecimal.valueOf(0.00):weight);
            userExtDao.insert(userExt);
        }else{
            userExt.setAddress(address);
            userExt.setHeight(height==null?userExt.getHeight():height);
            userExt.setWeight(weight==null?userExt.getWeight():weight);
            userExtDao.update(userExt);
        }

        user.setAge(age==null?user.getAge():age);
        userDao.update(user);

        if(id%2 != 0){
            throw new BizException("id为单数的用户不能更新，用以测试事务");
        }
        return true;
    }
    /**-----------------------------------用以测试RocketMQ消息的发送和消费 END---------------------------------*/

}
