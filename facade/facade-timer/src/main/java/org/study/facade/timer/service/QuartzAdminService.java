package org.study.facade.timer.service;

import org.study.common.statics.exceptions.BizException;

/**
 * Quartz实例管理接口，需要消费端启动 cluster="broadcast" 来配合使用
 */
public interface QuartzAdminService {
    /**
     * 暂停当前实例，如果暂停失败，则抛出异常
     *
     * 注意：
     *     1、此方法会挂起当前实例的所有任务，但应用并不会关闭
     *     2、当前实例的挂起状态只维护在当前内存中，如果应用重启，挂起设置会失效
     *     3、如果当前应用采用集群方式部署了多台机器，若dubbo消费端想要挂起所有实例，需要dubbo的服务提供方设置 @Service(cluster = "broadcast") 广播模式，
     *        广播模式 + 消费端重试 的情况下在绝大部分情况下都能够完成所有运行实例的挂起，但若遇到个别应用比较极端的情况(如：网络波动较大)，广播模式
     *        也不能100%的保证能挂起所有实例，这个需要评估对业务的影响
     *     4、鉴于第3点中提到的没有100%保证能挂起所有实例的情况，因为本应用足够轻便，单实例部署已足够，若为了故障容忍，部署2台亦可
     * @return
     */
    public void pauseInstance();

    /**
     * 恢复当前实例，如果恢复失败，则抛出异常
     * @return
     */
    public void resumeInstance();

    /**
     * 断言实例是暂停中，如果当前实例为非暂停中，则会抛出异常
     */
    public void assertPausing() throws BizException;

    /**
     * 断言实例是运行中，如果当前实例为暂停中，则会抛出异常
     */
    public void assertRunning() throws BizException;
}
