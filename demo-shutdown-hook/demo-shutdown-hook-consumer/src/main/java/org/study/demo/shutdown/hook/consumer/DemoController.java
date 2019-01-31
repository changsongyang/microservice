package org.study.demo.shutdown.hook.consumer;

import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.study.demo.shutdown.hook.consumer.config.EnvConfig;
import org.study.demo.shutdown.hook.provider.facade.ShutdownHookFacade;
import org.study.demo.shutdown.hook.provider.vo.HelloVo;

import java.util.Random;

@RestController
@RequestMapping("demo")
public class DemoController {
    @Reference
    ShutdownHookFacade shutdownHookFacade;

    @Autowired
    EnvConfig envConfig;

    @ResponseBody
    @RequestMapping(value = "/shutdownTest", method = RequestMethod.GET)
    public void shutdownTest(boolean isReset) {
        long now = System.currentTimeMillis();
        long i=0, max = envConfig.getCallTimes();
        boolean isException = false;
        while(i < max && isException==false){
            i++;
            try{
                isException = shutdownHookFacade.shutdownTest(i, "第" + i + "次调用", isReset);
                isReset = false;
            }catch(Throwable e){
                isException = true;
                e.printStackTrace();
            }
            System.out.println("===========>[DONE] callTimes="+i+",isException="+isException);
        }
        long costSec = (System.currentTimeMillis() - now) / 1000;
        System.out.println("===========>[END] callTimes="+i+",costSecond=" + costSec);
    }

    @ResponseBody
    @RequestMapping(value = "/sayHello2", method = RequestMethod.GET)
    public HelloVo sayHello2(String content) {
        HelloVo helloVo = new HelloVo();
        helloVo.setContent(content);
        helloVo.setCount(new Random().nextInt(20));
        helloVo.setDescription("测试一下");
        return shutdownHookFacade.syaHello(helloVo);
    }
}
