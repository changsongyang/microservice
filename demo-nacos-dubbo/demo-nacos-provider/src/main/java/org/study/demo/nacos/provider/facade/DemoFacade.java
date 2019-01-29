package org.study.demo.nacos.provider.facade;

import org.study.demo.nacos.provider.vo.HelloVo;

public interface DemoFacade {
    public String syaHello(String content);

    public HelloVo syaHello(HelloVo helloVo);
}
