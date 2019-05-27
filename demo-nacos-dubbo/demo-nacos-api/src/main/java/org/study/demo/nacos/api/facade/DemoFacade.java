package org.study.demo.nacos.api.facade;

import org.study.demo.nacos.api.vo.HelloVo;

public interface DemoFacade {
    public String syaHello(String content);

    public HelloVo syaHello(HelloVo helloVo);
}
