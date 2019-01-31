package org.study.demo.shutdown.hook.provider.facade;

import org.study.demo.shutdown.hook.provider.vo.HelloVo;

public interface ShutdownHookFacade {
    public boolean shutdownTest(long callTimes, String content, boolean isReset);

    public HelloVo syaHello(HelloVo helloVo);
}
