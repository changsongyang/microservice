package org.study.demo.rocketmq;

import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.study.common.util.utils.JsonUtil;
import org.study.common.util.utils.RandomUtil;
import org.study.demo.rocketmq.consts.Const;
import org.study.demo.rocketmq.vo.bizVo.ItemVo;
import org.study.demo.rocketmq.vo.bizVo.OrderVo;
import org.study.starter.component.RocketMQSender;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("demo")
public class DemoController {
    @Autowired
    RocketMQSender rmqSender;
    @Autowired
    JmsTemplate jmsTemplate;

    int messageCount = 1;
    int threadCount = 10;

    @RequestMapping(value = "/sendAmq")
    public boolean sendAmq(String topic, String trxNo){
        String tags = "oneTag";
        OrderVo vo = new OrderVo();
        vo.setTopic(topic);
        vo.setTags(tags);
        vo.setMsgType(10001);
        vo.setTrxNo(trxNo);
        vo.setAmount(BigDecimal.valueOf(20.36));
        vo.setIsFinish(true);

        long start = System.currentTimeMillis();
        for(int i=0; i<messageCount; i++){
            jmsTemplate.send("queue.name.ongTag", new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createTextMessage(JsonUtil.toString(vo));
                }
            });
        }
        long timeCost = ((System.currentTimeMillis()-start))/1000;
        System.out.println("发送结束 sendAmq timeCost="+timeCost);
        return true;
    }

    @RequestMapping(value = "/sendAmqVTopic")
    public String sendAmqVTopic(String topic, String trxNo){
        String tags = "oneTag";
        OrderVo vo = new OrderVo();
        vo.setTopic(topic);
        vo.setTags(tags);
        vo.setMsgType(10001);
        vo.setTrxNo(trxNo);
        vo.setAmount(BigDecimal.valueOf(20.36));
        vo.setIsFinish(true);

        long start = System.currentTimeMillis();
        for(int i=0; i<messageCount; i++){
            jmsTemplate.convertAndSend(new ActiveMQTopic("VirtualTopic.Orders"), JsonUtil.toString(vo));
        }
        long timeCost = ((System.currentTimeMillis()-start))/1000;
        System.out.println("发送结束 sendAmq timeCost="+timeCost);
        return "ok";
    }

    @RequestMapping(value = "/sendMuch")
    public boolean sendMuch(String topic, String trxNo, String type){
        String tags = "oneTag";
        OrderVo vo = new OrderVo();
        vo.setTopic(topic);
        vo.setTags(tags);
        vo.setMsgType(10001);
        vo.setTrxNo(trxNo);
        vo.setAmount(BigDecimal.valueOf(20.36));
        vo.setIsFinish(true);

        AtomicLong curCount = new AtomicLong(0);

        //开多线程，加快发送消息的速度
        long start = System.currentTimeMillis();
        for(int i=0; i<threadCount; i++){
            if("amq".equals(type)){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(true){
                            if(curCount.incrementAndGet() >= messageCount){
                                break;
                            }
                            jmsTemplate.send("queue.name.ongTag", new MessageCreator() {
                                @Override
                                public Message createMessage(Session session) throws JMSException {
                                    return session.createTextMessage(JsonUtil.toString(vo));
                                }
                            });
                            System.out.println("发送完成 sendMuchAmq curCount="+curCount.get());
                        }

                        long timeCost = ((System.currentTimeMillis()-start))/1000;
                        System.out.println("发送结束 sendMuchAmq curCount="+curCount.get()+" timeCost="+timeCost);
                    }
                }).start();
            }else if("rmq".equals(type)){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(true){
                            if(curCount.incrementAndGet() >= messageCount){
                                break;
                            }
                            rmqSender.sendOne(vo);
                            System.out.println("发送完成 sendMuchRmq curCount="+curCount.get());
                        }

                        long timeCost = ((System.currentTimeMillis()-start))/1000;
                        System.out.println("发送结束 sendMuchRmq curCount="+curCount.get()+" timeCost="+timeCost);
                    }
                }).start();
            }
        }
        return true;
    }

    @RequestMapping(value = "/sendOne")
    public boolean sendOne(String topic, String trxNo, String param) {
        String tags = "oneTag";

        OrderVo vo = new OrderVo();
        vo.setTopic(topic);
        vo.setTags(tags);
        vo.setMsgType(10001);
        vo.setTrxNo(trxNo);

        vo.setAmount(BigDecimal.valueOf(20.36));
        vo.setIsFinish(true);
        vo.setJsonParam(param);

        System.out.println("发送开始 sendOne");
        long start = System.currentTimeMillis();
        rmqSender.sendOne(vo);
        long timeCost = ((System.currentTimeMillis()-start));
        System.out.println("发送结束 sendOne timeCost="+timeCost);
        return true;
    }

    @RequestMapping(value = "/sendBatch")
    public boolean sendBatch(String topic, String trxNo) {
        String tags = "batchTags";
        List<OrderVo> voList = new ArrayList<>();
        for(int i=1; i<=3; i++){
            OrderVo vo = new OrderVo();
            vo.setTopic(topic);
            vo.setTags(tags+"_"+i);
            vo.setMsgType(10002);
            vo.setTrxNo(trxNo + i);

            vo.setAmount(BigDecimal.valueOf(10.01 * i).setScale(2, BigDecimal.ROUND_DOWN));
            vo.setIsFinish(true);
            voList.add(vo);
        }
        rmqSender.sendBatch(voList);
        return true;
    }

    @RequestMapping(value = "/sendBatch2")
    public boolean sendBatch2(String topic, String tags, String trxNo) {
        List<OrderVo> voList = new ArrayList<>();
        for(int i=1; i<=3; i++){
            OrderVo vo = new OrderVo();
            vo.setTopic(topic);
            vo.setMsgType(10002);
            vo.setTrxNo(trxNo + i);

            vo.setAmount(BigDecimal.valueOf(10.01 * i).setScale(2, BigDecimal.ROUND_DOWN));
            vo.setIsFinish(true);
            voList.add(vo);
        }
        rmqSender.sendBatch(topic + ":" + tags, voList);
        return true;
    }

    @RequestMapping(value = "/sendTrans")
    public boolean sendTrans(String topic, String trxNo) {
        String tags = "transTag";

        int ranVal = RandomUtil.getInt(2);

        OrderVo vo = new OrderVo();
        vo.setTopic(topic);
        vo.setTags(tags);
        vo.setMsgType(ranVal == 1 ? 20001 : 20002);
        vo.setTrxNo(trxNo);

        vo.setAmount(BigDecimal.valueOf(368.52));
        vo.setIsFinish(true);

        System.out.println("发送开始 sendTrans");
        long start = System.currentTimeMillis();
        rmqSender.sendTrans(Const.TX_PRODUCER_GROUP, vo);
        long timeCost = ((System.currentTimeMillis()-start));
        System.out.println("发送结束 sendTrans timeCost="+timeCost);
        return true;
    }

    @RequestMapping(value = "/sendTransItem")
    public boolean sendTransItem(String topic, String trxNo) {
        String tags = "transTagItem";

        int ranVal = RandomUtil.getInt(2);

        OrderVo vo = new OrderVo();
        vo.setTopic(topic);
        vo.setTags(tags);
        vo.setMsgType(ranVal == 1 ? 20001 : 20002);
        vo.setTrxNo(trxNo);
        vo.setAmount(BigDecimal.valueOf(3852.32));
        vo.setIsFinish(true);

        List<ItemVo> itemVos = new ArrayList<>();
        for(int i=1; i<=5; i++){
            ItemVo itemVo = new ItemVo();
            itemVo.setItemNo(trxNo+"_"+i);
            itemVo.setItemAmount(BigDecimal.valueOf(5 * i));
            itemVos.add(itemVo);
        }
        vo.setItemVoList(itemVos);

        System.out.println("发送开始 sendTransItem");
        long start = System.currentTimeMillis();
        rmqSender.sendTrans(Const.TX_PRODUCER_GROUP, vo);
        long timeCost = ((System.currentTimeMillis()-start));
        System.out.println("发送结束 sendTransItem timeCost="+timeCost);
        return true;
    }
}
