package org.study.service.timer.core.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.study.service.timer.core.job.base.AutowiringSpringBeanJobFactory;
import org.study.service.timer.core.job.listener.JobListener;
import org.study.service.timer.core.job.listener.SchedulerListener;
import org.study.service.timer.core.job.listener.TriggerListener;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author chenyf on 2017/3/9.
 */
@SpringBootConfiguration
public class QuartzConfig {

    /**
     * spring用来和Quartz交互的对象
     * https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/quartz/SchedulerFactoryBean.html
     * @return
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(PlatformTransactionManager transactionManager, DataSource dataSource){
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setJobFactory(jobFactory());
        //设置数据源
        schedulerFactoryBean.setDataSource(dataSource);
        //设置事务管理器
        schedulerFactoryBean.setTransactionManager(transactionManager);
        schedulerFactoryBean.setQuartzProperties(quartzProperties());
        schedulerFactoryBean.setOverwriteExistingJobs(false);
        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);
        schedulerFactoryBean.setAutoStartup(true);
        //设置全局trigger监听器
        schedulerFactoryBean.setGlobalTriggerListeners(triggerListener());
        //设置全局job监听器
        schedulerFactoryBean.setGlobalJobListeners(jobListener());
        schedulerFactoryBean.setSchedulerListeners(schedulerListener());
        return schedulerFactoryBean;
    }

    /**
     * 定义一个名为jobFactory的任务工厂，Quartz默认是使用这个工厂
     * @return
     */
    @Bean
    public AutowiringSpringBeanJobFactory jobFactory(){
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        return jobFactory;
    }

    /**
     * quartz的配置属性
     * @return
     */
    @Bean
    public Properties quartzProperties(){
        Properties properties = new Properties();
        //实例配置
        //实例名称，同一个集群里面的所有实例名称要相同
        properties.put("org.quartz.scheduler.instanceName", "serviceTimer");
        //实例ID，一个集群里面的所有实例ID要不同，配置为自动即可
        properties.put("org.quartz.scheduler.instanceId", "AUTO");

        //集群配置
        //设置为集群模式
        properties.put("org.quartz.jobStore.isClustered", "true");
        //集群检入间隔(毫秒)
        properties.put("org.quartz.jobStore.clusterCheckinInterval", "5000");
        //处理misfire的最大线程数
        properties.put("org.quartz.jobStore.maxMisfiresToHandleAtATime", "5");
        //任务被判定为misfire的等待时间(毫秒)
        properties.put("org.quartz.jobStore.misfireThreshold", "6000");

        //线程池配置
        properties.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        properties.put("org.quartz.threadPool.threadCount", "100");
        properties.put("org.quartz.threadPool.threadPriority", "5");

        //JobStore
        //让quartz的事务交由spring来管理
        properties.put("org.quartz.jobStore.class", "org.springframework.scheduling.quartz.LocalDataSourceJobStore");
        properties.put("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        //quartz数据库表前缀
        properties.put("org.quartz.jobStore.tablePrefix", "QRTZ_");
        //org.quartz.jobStore.useProperties设置为true时jobDetail的jobDataMap里面必须为字符串类型
        properties.put("org.quartz.jobStore.useProperties", "true");
        return properties;
    }

    /**
     * 定义一个任务监听器
     * @return
     */
    @Bean
    public JobListener jobListener(){
        return new JobListener();
    }

    /**
     * 定义一个触发器监听器
     * @return
     */
    @Bean
    public TriggerListener triggerListener(){
        return new TriggerListener();
    }

    @Bean
    public SchedulerListener schedulerListener(){
        return new SchedulerListener();
    }
}
