package com.xpay.service.timer.job.base;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * @author chenyf on 2017/12/1.
 * 继承自SpringBeanJobFactory，目的是为了当Quartz为Job的实现类进行实例化时，能够为其进行依赖注入
 */
public class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory {

    @Override
    public Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
        final Object job = super.createJobInstance(bundle);
        //进行依赖注入(只能注入本地Spring中的bean)
//        applicationContext.getAutowireCapableBeanFactory().autowireBean(job);
        return job;
    }
}
