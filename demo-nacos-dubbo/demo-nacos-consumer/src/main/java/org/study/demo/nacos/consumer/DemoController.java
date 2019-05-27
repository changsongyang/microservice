package org.study.demo.nacos.consumer;

import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.study.demo.nacos.api.facade.DemoFacade;
import org.study.demo.nacos.api.vo.HelloVo;

import java.util.Random;

@RestController
@RequestMapping("demo")
public class DemoController {
    @Reference
    DemoFacade demoFacade;

    @ResponseBody
    @RequestMapping(value = "/sayHello", method = RequestMethod.GET)
    public String sayHello(String content) {
        return demoFacade.syaHello(content);
    }

    @ResponseBody
    @RequestMapping(value = "/sayHello2", method = RequestMethod.GET)
    public HelloVo sayHello2(String content) {
        HelloVo helloVo = new HelloVo();
        helloVo.setContent(content);
        helloVo.setCount(new Random().nextInt(20));
        helloVo.setDescription("测试一下");
        return demoFacade.syaHello(helloVo);
    }
}
