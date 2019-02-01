package org.study.demo.hot.deploy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.study.common.util.component.RmqSender;
import org.study.common.util.vo.MessageVo;
import org.study.demo.hot.deploy.config.CommonProperties;
import org.study.demo.hot.deploy.config.DataSourceProperties;
import org.study.demo.hot.deploy.dao.User;
import org.study.demo.hot.deploy.dao.UserDao;

import javax.sql.DataSource;
import java.util.Random;

@RestController
@RequestMapping("demo")
public class DemoController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    UserDao userDao;
    @Autowired
    RmqSender rmqSender;
    @Autowired
    CommonProperties propertiesConfig;
    @Autowired
    DataSourceProperties dataSourceProperties;

    /**
     * 数据库热更新
     *    执行方式：
     *        1、准备两个数据库，里面的表结构一样，例如：192.168.101.10:3360、192.168.101.11:3360
     *        2、先在在nacos配置中把数据库地址指向192.168.101.10，然后就调用本方法进行更新操作
     *        3、在更新的过程中，到nacos控制台把数据库地址更新为192.168.101.11
     *        4、观察地址切换过去之后192.168.101.10已经无数据更新，而192.168.101.11则开始更新数据，并且切换的过程中没有数据更新丢失
     *
     * @param max
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/dbHotUpdate")
    public boolean dbHotUpdate(int max, boolean isReset) {
        Long id = 2L;
        User user = new User();
        if(isReset){
            userDao.reset(id);
        }

        for(int i=1; i <= max; i++){
            user.setId(id);
            user.setUsername("第"+i+"次更新,flag="+propertiesConfig.getHotDeployFlag());

            try{
                userDao.update(user);
                String cfgUrl = dataSourceProperties.getUrl().substring(30, 35);
                log.info("========>[END] the {} time update success cfgUrl={}", i, cfgUrl);
            }catch (Throwable e){
                log.error("========>[EXCEPTION] the {} time update Exception", i, e);
            }
        }
        return true;
    }

    /**
     * MQ热更新
     *    执行方式：
     *        1、准备两个Broker和name Server实例，例如：192.168.101.10:9876、192.168.101.11:9876
     *        2、先在在nacos配置中把name-server地址指向192.168.101.10:9876，然后就调用本方法进行发送消息
     *        3、在发送消息的过程中，到nacos控制台把name-server地址更新为192.168.101.11:9876
     *        4、观察地址切换过去之后192.168.101.10已经消息新增，而192.168.101.11则开始新增消息数据，并且切换的过程中没有消息丢失
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/mqHotUpdate")
    public boolean mqHotUpdate(int max) {
        String topic = "my-topic";
        String tags = "oneTag";
        String msgKey = "36665214565521122";

        Random random = new Random();
        for(int i=1; i<=max; i++){
            MessageVo vo = new MessageVo();
            vo.setTopic(topic);
            vo.setTags(tags);
            vo.setMsgType(60001);
            vo.setTrxNo(msgKey+random.nextInt(3000));
            vo.setMsgKey(vo.getTrxNo());

            try{
                rmqSender.sendOne(vo);
                log.info("========>[END] the {} time send message success", i);
            }catch (Throwable e){
                log.error("========>[EXCEPTION] the {} time send message Exception", i, e);
            }
        }

        return true;
    }
}
