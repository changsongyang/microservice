package org.study.demo.sentinel.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.study.demo.sentinel.service.TestService;

@RestController
@RequestMapping("demo")
public class DemoController {
    @Autowired
    private TestService testService;

    @RequestMapping(value = "/qps")
    public boolean qps(String desc){
        for(int i=1; i<=100; i++){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Integer index = Integer.valueOf(Thread.currentThread().getName());
                    testService.qps(index, desc);
                }
            });
            thread.setName(String.valueOf(i));
            thread.start();
        }
        return true;
    }
}
