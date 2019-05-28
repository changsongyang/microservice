package org.study.common.util.component;

import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import javax.annotation.PostConstruct;
import java.util.Map;

public class ShutdownHook {
    private Logger logger = LoggerFactory.getLogger(ShutdownHook.class);
    private long dubboShutdownTimeOutMills = 10000;//dubbo框架关闭的超时时间
    @Autowired
    private GenericApplicationContext genericApplicationContext;

    public ShutdownHook() {
        registerShutdownHook();
    }

    private void registerShutdownHook(){
        Thread shutdownHook = new Thread() {
            public void run() {
                try {
                    //step 1：关闭RocketMQ的消费者
                    destroyRocketMQConsumer();

                    //step 2：把Dubbo提供者从注册中心注销
                    unregisterDubboProvider();

                    //step 3：关闭Spring容器
                    SpringApplication.exit(ShutdownHook.this.genericApplicationContext);
                } finally {
                    removeShutdownHook(this);
                }
            }
        };

        logger.info("Register shutdownHook to JVM, threadId: {} threadName: {}", shutdownHook.getId(), shutdownHook.getName());
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    private void destroyRocketMQConsumer(){
        try {
            Map<String, DefaultRocketMQListenerContainer> consumerContainer = genericApplicationContext.getBeansOfType(DefaultRocketMQListenerContainer.class);
            if(consumerContainer == null || consumerContainer.isEmpty()){
                return;
            }

            for(Map.Entry<String, DefaultRocketMQListenerContainer> entry : consumerContainer.entrySet()){
                try{
                    entry.getValue().destroy();
                }catch(Throwable e){
                    logger.error("beanName = {} shutdown Exception", entry.getKey(), e);
                }
            }
        } catch (Throwable e) {
            logger.error("Exception Happen While RocketMQConsumer shutting down", e);
        }
    }

    private void unregisterDubboProvider(){
        try {

        } catch (Throwable e) {
            logger.error("Exception Happen While DubboProvider unregister", e);
        }
    }

    private void removeShutdownHook(Thread thread){
        logger.info("Remove shutdownHook from JVM, threadId: {} threadName: {}", thread.getId(), thread.getName());
        try{
            Runtime.getRuntime().removeShutdownHook(thread);
        }catch (IllegalStateException e){
        }
    }

    public long getDubboShutdownTimeOutMills() {
        return dubboShutdownTimeOutMills;
    }

    public void setDubboShutdownTimeOutMills(long dubboShutdownTimeOutMills) {
        this.dubboShutdownTimeOutMills = dubboShutdownTimeOutMills;
    }
}
