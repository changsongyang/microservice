package org.study.timer.provider.core.job.base;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * @author chenyf on 2017/12/1.
 * 继承自SpringBeanJobFactory，目的是为了当Quartz为Job的实现类进行实例化时，能够为其进行依赖注入
 */
public class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory {
    @Autowired
    private transient AutowireCapableBeanFactory beanFactory;

    @Override
    public Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
        final Object job = super.createJobInstance(bundle);
        //进行依赖注入(只能注入本地Spring中的bean)
        beanFactory.autowireBean(job);
        return job;
    }
}
