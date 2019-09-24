package org.study.demo.sentinel.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.springframework.stereotype.Service;

@Service
public class TestServiceImpl implements TestService {

    @SentinelResource(value = "qpsResource", blockHandler = "qpsBlock", blockHandlerClass = {BlockHandler.class})
    public String qps(int index, String desc){
        try{
            Thread.sleep(500);
            System.out.println("==>TestService.qps index="+index+",desc = " + desc);
        }catch (Exception e){

        }
        return null;
    }
}
