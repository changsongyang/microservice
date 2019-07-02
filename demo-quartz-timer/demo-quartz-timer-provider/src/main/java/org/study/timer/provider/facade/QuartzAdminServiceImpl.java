package org.study.timer.provider.facade;

import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.study.timer.provider.core.biz.InstanceStageBiz;
import org.study.timer.provider.service.QuartzAdminService;

@Service(cluster = "broadcast")//需要设置dubbo的协议为'广播模式'
public class QuartzAdminServiceImpl implements QuartzAdminService {
    @Autowired
    InstanceStageBiz instanceStageBiz;

    /**
     * 暂停当前实例，如果暂停失败，则抛出异常
     * @return
     */
    @Override
    public void pauseInstance(){
        instanceStageBiz.pauseInstance();
    }

    /**
     * 恢复当前实例，如果恢复失败，则抛出异常
     * @return
     */
    @Override
    public void resumeInstance(){
        instanceStageBiz.resumeInstance();
    }

    /**
     * 断言实例是暂停中，如果当前实例为非暂停中，则会抛出异常
     */
    @Override
    public void assertPausing(){
        instanceStageBiz.assertPausing();
    }

    /**
     * 断言实例是运行中，如果当前实例为暂停中，则会抛出异常
     */
    @Override
    public void assertRunning(){
        instanceStageBiz.assertRunning();
    }
}
