package org.study.demo.sentinel.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.stereotype.Service;
@Service
public class TestServiceImpl implements TestService {

//    @SentinelResource(value = "qpsResource", blockHandler = "qpsBlock", blockHandlerClass = {BlockHandler.class})//测试异常处理器时开启
    @SentinelResource(value = "qpsResource")//测试不配置任何异常处理器时开启
    public boolean qps(int index) throws BlockException {//使用注解时，如果希望遇到限流时抛出异常，必须在此声明 BlockException，不然Spring会抛出UndeclaredThrowableException
        System.out.println("==>TestService.qps index="+index);
        return true;
    }

//    @SentinelResource(value = "degradeResource", blockHandler = "degradeBlock", blockHandlerClass = {BlockHandler.class})//测试异常处理器时开启
    @SentinelResource(value = "degradeResource")//测试不配置任何异常处理器时开启
    public boolean degrade(int index) throws BlockException {
        if(index%2 == 0){
            try{
                Thread.sleep(5 * 1000);
            }catch (InterruptedException e){

            }
        }else{
            System.out.println("==>TestService.degrade index="+index);
        }
        return true;
    }
}
