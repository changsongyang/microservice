package org.study.demo.rocketmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.study.common.util.component.RocketMQSender;
import org.study.common.util.utils.RandomUtil;
import org.study.demo.rocketmq.consts.Const;
import org.study.demo.rocketmq.vo.bizVo.ItemVo;
import org.study.demo.rocketmq.vo.bizVo.OrderVo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("demo")
public class DemoController {
    @Autowired
    RocketMQSender rmqSender;

    @ResponseBody
    @RequestMapping(value = "/sendOne")
    public boolean sendOne(String topic, String msgKey) {
        String tags = "oneTag";

        OrderVo vo = new OrderVo();
        vo.setTopic(topic);
        vo.setTags(tags);
        vo.setMsgType(10001);
        vo.setTrxNo(msgKey);

        vo.setAmount(BigDecimal.valueOf(20.36));
        vo.setIsFinish(true);
        rmqSender.sendOne(vo);
        return true;
    }

    @ResponseBody
    @RequestMapping(value = "/sendBatch")
    public boolean sendBatch(String topic, String msgKey) {
        String tags = "batchTag";

        List<OrderVo> voList = new ArrayList<>();
        for(int i=1; i<=3; i++){
            OrderVo vo = new OrderVo();
            vo.setTopic(topic);
            vo.setTags(tags+"_"+i);
            vo.setMsgType(10002);
            vo.setTrxNo(msgKey + i);

            vo.setAmount(BigDecimal.valueOf(10.01 * i).setScale(2, BigDecimal.ROUND_DOWN));
            vo.setIsFinish(true);
            voList.add(vo);
        }
        rmqSender.sendBatch(topic, voList);
        return true;
    }

    @ResponseBody
    @RequestMapping(value = "/sendTrans")
    public boolean sendTrans(String topic, String msgKey) {
        String tags = "transTag";

        int ranVal = RandomUtil.getInt(2);

        OrderVo vo = new OrderVo();
        vo.setTopic(topic);
        vo.setTags(tags);
        vo.setMsgType(ranVal == 1 ? 20001 : 20002);
        vo.setTrxNo(msgKey);

        vo.setAmount(BigDecimal.valueOf(368.52));
        vo.setIsFinish(true);
        rmqSender.sendTrans(Const.TX_PRODUCER_GROUP, vo);
        return true;
    }

    @ResponseBody
    @RequestMapping(value = "/sendTransItem")
    public boolean sendTransItem(String topic, String msgKey) {
        String tags = "transTagItem";

        int ranVal = RandomUtil.getInt(2);

        OrderVo vo = new OrderVo();
        vo.setTopic(topic);
        vo.setTags(tags);
        vo.setMsgType(ranVal == 1 ? 20001 : 20002);
        vo.setTrxNo(msgKey);
        vo.setAmount(BigDecimal.valueOf(3852.32));
        vo.setIsFinish(true);

        List<ItemVo> itemVos = new ArrayList<>();
        for(int i=1; i<=5; i++){
            ItemVo itemVo = new ItemVo();
            itemVo.setItemNo(msgKey+"_"+i);
            itemVo.setItemAmount(BigDecimal.valueOf(5 * i));
            itemVos.add(itemVo);
        }
        vo.setItemVoList(itemVos);

        rmqSender.sendTrans(Const.TX_PRODUCER_GROUP, vo);
        return true;
    }
}
