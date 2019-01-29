package org.study.demo.nacos.provider.service;

import com.alibaba.dubbo.config.annotation.Service;
import org.study.demo.nacos.provider.facade.DemoFacade;
import org.study.demo.nacos.provider.vo.HelloVo;

import java.util.Random;

@Service
public class DemoFacadeImpl implements DemoFacade {
    public String syaHello(String content){
        return "Hello World: " + content;
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
