package org.study.demo.rocketmq;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("demo")
public class DemoController {
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @ResponseBody
    @RequestMapping(value = "/sendOne")
    public boolean sendOne(String topic) {
        rocketMQTemplate.send(topic, MessageBuilder.withPayload("Hello, World! I'm from spring message sendOne").build());
        return true;
    }

}
