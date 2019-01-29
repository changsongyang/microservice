package org.study.demo.rocketmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.study.demo.rocketmq.component.Sender;
import org.study.demo.rocketmq.consts.Const;
import org.study.demo.rocketmq.vo.ItemVo;
import org.study.demo.rocketmq.vo.OrderVo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("demo")
public class DemoController {
    @Autowired
    Sender sender;

    @ResponseBody
    @RequestMapping(value = "/sendOne")
    public boolean sendOne(String topic, String msgKey) {
        String tags = "oneTag";

        OrderVo vo = new OrderVo();
        vo.setAmount(BigDecimal.valueOf(20.36));
        vo.setIsFinish(true);
        vo.setTrxNo(msgKey);
        vo.setMsgType(10001);

        sender.sendOne(topic, tags, msgKey, vo);
        return true;
    }

    @ResponseBody
    @RequestMapping(value = "/sendBatch")
    public boolean sendBatch(String topic, String msgKey) {
        String tags = "batchTag";

        List<OrderVo> voList = new ArrayList<>();
        for(int i=1; i<=3; i++){
            OrderVo vo = new OrderVo();
            vo.setAmount(BigDecimal.valueOf(10.01 * i).setScale(2, BigDecimal.ROUND_DOWN));
            vo.setIsFinish(true);
            vo.setTrxNo(msgKey);
            vo.setMsgType(10002);
            voList.add(vo);
        }
        sender.sendBatch(topic, tags, msgKey, voList);
        return true;
    }

    @ResponseBody
    @RequestMapping(value = "/sendTrans")
    public boolean sendTrans(String topic, String msgKey) {
        String tags = "transTag";

        OrderVo vo = new OrderVo();
        vo.setAmount(BigDecimal.valueOf(368.52));
        vo.setIsFinish(true);
        vo.setTrxNo(msgKey);
        vo.setMsgType(20001);

        sender.sendTrans(Const.TX_PRODUCER_GROUP, topic, tags, msgKey, vo);
        return true;
    }

    @ResponseBody
    @RequestMapping(value = "/sendTransItem")
    public boolean sendTransItem(String topic, String msgKey) {
        String tags = "transTagItem";

        OrderVo vo = new OrderVo();
        vo.setAmount(BigDecimal.valueOf(3852.32));
        vo.setIsFinish(true);
        vo.setTrxNo(msgKey);
        vo.setMsgType(20002);

        List<ItemVo> itemVos = new ArrayList<>();
        for(int i=1; i<=5; i++){
            ItemVo itemVo = new ItemVo();
            itemVo.setItemNo(msgKey+"_"+i);
            itemVo.setItemAmount(BigDecimal.valueOf(5 * i));
            itemVos.add(itemVo);
        }
        vo.setItemVoList(itemVos);

        sender.sendTrans(Const.TX_PRODUCER_GROUP, topic, tags, msgKey, vo);
        return true;
    }

}
