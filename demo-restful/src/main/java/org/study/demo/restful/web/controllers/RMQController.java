package org.study.demo.restful.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.study.common.statics.constants.MsgEvent;
import org.study.common.statics.pojos.RestResult;
import org.study.common.mq.message.PMessage;
import org.study.common.mq.producer.Producer;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("rmq")
@RestController
public class RMQController {
    @Autowired
    Producer producer;

    @RequestMapping(value = "sendOne", method = RequestMethod.POST)
    public RestResult sendOne(String msgKey, String msgBody){
        PMessage message = new PMessage();
        message.setTopic("singleMsgTopic");
        message.setTags("oneTag");
        message.setKey(msgKey);
        message.setBody(msgBody);
        message.setMsgEvent(MsgEvent.SINGLE_MSG_TEST);
        producer.send(message);
        return RestResult.bizSuccess(100, "发送单笔消息成功");
    }

    @RequestMapping(value = "sendBatch", method = RequestMethod.POST)
    public RestResult sendBatch(String msgKey, String msgBody, Integer count){
        List<PMessage<String>> messageList = new ArrayList<>();
        if(count == null){
            count = 100;
        }else if(count > 10000){//避免超过单次发消息的最大尺寸限制
            count = 10000;
        }
        for(int i=0; i<count; i++){
            PMessage message = new PMessage();
            message.setTopic("batchMsgTopic");
            message.setTags("oneTag");
            message.setKey(msgKey + i);
            message.setBody(msgBody + "_" + i);
            message.setMsgEvent(MsgEvent.BATCH_MSG_TEST);
            messageList.add(message);
        }

        producer.sendBatch(messageList.get(0).getTopic(), messageList);

        return RestResult.bizSuccess(100, "发送批量消息成功");
    }

    @RequestMapping(value = "sendTransaction", method = RequestMethod.POST)
    public RestResult sendTransaction(String msgKey, String msgBody, Integer type){
        if(type == null) type = 1;

        PMessage message = new PMessage();
        message.setTopic("transMsgTopic");
        message.setTags("oneTag");
        message.setKey(msgKey);
        message.setBody(msgBody);
        message.setMsgEvent(type.equals(2) ? MsgEvent.TRANS_MSG_TEST_2 : MsgEvent.TRANS_MSG_TEST_1);
        producer.sendTransaction(message);

        return RestResult.bizSuccess(100, "发送事务消息成功");
    }

    @RequestMapping(value = "sendMuch", method = RequestMethod.POST)
    public RestResult sendMuch(String msgKey, String msgBody){
        PMessage message = new PMessage();
        message.setTopic("singleMsgTopic");
        message.setTags("muchTag");
        message.setKey(msgKey);
        message.setBody(msgBody);
        message.setMsgEvent(MsgEvent.SINGLE_MSG_TEST);

        int maxThread = 50, maxNum = 1000;
        for(int i=0; i<=maxThread; i++){
            final int v = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int j=0; j <= maxNum; j++) {
                        message.setKey(msgKey+"_"+v+"_"+j);
                        message.setBody(msgBody+"_i="+v+"_j="+j);
                        producer.send(message);
                    }

                }
            }).start();
        }

        return RestResult.bizSuccess(100, "发送多消息成功");
    }
}
