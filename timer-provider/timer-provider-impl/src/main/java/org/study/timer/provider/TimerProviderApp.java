package org.study.timer.provider;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author chenyf on 2017/8/20.
 */
@EnableTransactionManagement
@SpringBootApplication
public class TimerProviderApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(TimerProviderApp.class).web(WebApplicationType.NONE).run(args);
    }
}
