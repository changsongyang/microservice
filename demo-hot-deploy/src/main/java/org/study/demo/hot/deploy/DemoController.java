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
    DataSource dataSource;
    @Autowired
    DataSourceProperties dataSourceProperties;

    /**
     * 数据库热更新
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
                String datUrl = "";
//                String datUrl = ((DruidDataSource) dataSource).getUrl().substring(28, 33);
                log.info("========>[END] the {} time update success cfgUrl={} datUrl={}", i, cfgUrl, datUrl);
            }catch (Throwable e){
                log.error("========>[EXCEPTION] the {} time update Exception", i, e);
            }
        }
        return true;
    }

    /**
     * MQ热更新
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
