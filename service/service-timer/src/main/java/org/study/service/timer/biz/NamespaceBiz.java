package org.study.service.timer.biz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.study.common.statics.exceptions.BizException;
import org.study.common.util.utils.StringUtil;
import org.study.facade.timer.entity.Namespace;
import org.study.facade.timer.enums.TimerStatus;
import org.study.service.timer.config.TimerConfig;
import org.study.service.timer.dao.NamespaceDao;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class NamespaceBiz {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
    @Autowired
    TimerConfig timerConfig;
    @Autowired
    NamespaceDao namespaceDao;
    @Autowired
    InstanceBiz instanceBiz;

    public synchronized void initialize(){
        //1.初始化命名空间的记录
        String name = timerConfig.getNamespace();
        Namespace namespace = namespaceDao.getByPk(name);
        if(namespace == null){
            namespace = new Namespace();
            namespace.setNamespace(name);
            namespace.setStatus(TimerStatus.RUNNING.getValue());
            int count = namespaceDao.insert(namespace);
            if(count <= 0){
                throw new BizException("namespace="+name+"命名空间初始化失败");
            }
        }

        //2.初始化实例的记录
        instanceBiz.initialize(name, namespace.getStatus());

        //3.定时扫描命名空间的状态，并把此状态同步到当前实例状态上去
        int internal = timerConfig.getStageCheckMills();
        if(internal <= 1000){
            //避免轮训频率过高压垮数据库，如果是希望实现毫秒级的延迟，应该使用zk/redis等组件的发布-订阅模式来处理，因为不想引入太多组件，所以，此处并没有使用这些组件来实现
            throw new BizException("stageCheckMills = " + internal + " 必须大于1000");
        }
        scheduledExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try{
                    String name = timerConfig.getNamespace();
                    Namespace namespace = namespaceDao.getByPk(name);
                    if(namespace.getStatus() == TimerStatus.STAND_BY.getValue()){
                        instanceBiz.pause(name);
                    }else if(namespace.getStatus() == TimerStatus.RUNNING.getValue()){
                        instanceBiz.resume(name);
                    }
                }catch(Throwable e){
                    logger.error("namespace-instance状态检测同步时出现异常", e);
                }
            }
        }, internal, internal, TimeUnit.MILLISECONDS);
    }

    /**
     * 发送暂停实例的消息
     * @see #resumeNamespaceAsync(String)
     * @return
     */
    public boolean pauseNamespaceAsync(String namespace){
        if(StringUtil.isEmpty(namespace)){
            throw new BizException("namespace不能为空");
        }
        return namespaceDao.updateStatus(TimerStatus.STAND_BY.getValue(), namespace);
    }

    /**
     * 发送恢复实例的通知
     * @see #pauseNamespaceAsync(String)
     * @return
     */
    public boolean resumeNamespaceAsync(String namespace){
        if(StringUtil.isEmpty(namespace)){
            throw new BizException("namespace不能为空");
        }
        return namespaceDao.updateStatus(TimerStatus.RUNNING.getValue(), namespace);
    }

    public List<Namespace> listAllNamespace(){
        return namespaceDao.listAll();
    }
}
