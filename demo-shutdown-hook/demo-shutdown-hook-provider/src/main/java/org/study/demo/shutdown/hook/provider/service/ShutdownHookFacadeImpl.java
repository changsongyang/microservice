package org.study.demo.shutdown.hook.provider.service;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.study.demo.shutdown.hook.provider.dao.User;
import org.study.demo.shutdown.hook.provider.dao.UserDao;
import org.study.demo.shutdown.hook.provider.facade.ShutdownHookFacade;
import org.study.demo.shutdown.hook.provider.rmq.Sender;
import org.study.demo.shutdown.hook.provider.vo.HelloVo;
import org.study.demo.shutdown.hook.provider.vo.OrderVo;

import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ShutdownHookFacadeImpl implements ShutdownHookFacade {
    @Autowired
    UserDao userDao;
    @Autowired
    Sender sender;
    AtomicLong dbUpdateCount = new AtomicLong(0);
    AtomicLong mqSendCount = new AtomicLong(0);

    /**
     * 测试shutdown hook：先更新数据库、再发送消息，观察数据库更新次数和消息发送次数是否一致
     * @param content
     * @return
     */
    public boolean shutdownTest(long callTimes, String content, boolean isReset){
        boolean isException = false;
        try{
            Long id = 1L;
            if(isReset){
                dbUpdateCount.set(0);
                mqSendCount.set(0);
                userDao.reset(id);
            }

            content = "Hello World: " + content;

            User user = new User();
            user.setId(id);
            user.setUsername(content);
//            userDao.update(user);
//            dbUpdateCount.getAndIncrement();

            String topic = "my-topic";
            String tags = "oneTag";
            String msgKey = "20022019013152145236521";
            OrderVo vo = new OrderVo();
            vo.setTopic(topic);
            vo.setTags(tags);
            vo.setMsgType(10001);
            vo.setTrxNo(msgKey);
            vo.setMsgKey(msgKey);
            vo.setAmount(BigDecimal.valueOf(20.36));
            vo.setIsFinish(true);
            sender.sendOne(vo);
            mqSendCount.getAndIncrement();
        }catch(Throwable e){
            isException = true;
            e.printStackTrace();
        }

        System.out.println("===========>[END] callTimes="+callTimes+",isException="+isException+",dbUpdateCount="+dbUpdateCount.get()+",mqSendCount="+mqSendCount.get());
        return isException;
    }

    public HelloVo syaHello(HelloVo helloVo){
        HelloVo vo = new HelloVo();
        int count = new Random().nextInt(50);
        vo.setCount(count + helloVo.getCount());
        vo.setContent("Hello World: " + helloVo.getContent());
        vo.setDescription("这是一个Hello World样例 inputCount="+helloVo.getCount()+",newCount="+count);
        return vo;
    }
}
