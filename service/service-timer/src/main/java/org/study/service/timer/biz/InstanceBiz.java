package com.xpay.service.timer.biz;

import com.xpay.common.statics.exceptions.BizException;
import com.xpay.common.statics.result.PageParam;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.util.utils.IPUtil;
import com.xpay.common.util.utils.StringUtil;
import com.xpay.facade.timer.entity.Namespace;
import com.xpay.facade.timer.enums.TimerStatus;
import com.xpay.service.timer.dao.InstanceDao;
import com.xpay.service.timer.dao.NamespaceDao;
import com.xpay.service.timer.job.base.JobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.xpay.facade.timer.entity.Instance;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 实例状态逻辑层
 */
@Component
public class InstanceBiz {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 是否已完成初始化
     */
    private boolean isInitFinished = false;
    /**
     * 是否应该处于运行状态，此状态应该和数据库中Instance.status的状态保持一致
     */
    private boolean isRunning = false;

    @Autowired
    JobManager jobManager;
    @Autowired
    InstanceDao InstanceDao;
    @Autowired
    NamespaceDao namespaceDao;

    /**
     * 初始化
     */
    public synchronized void initialize(String namespace, int status){
        String instanceId = getInstanceId();
        Instance instance = InstanceDao.getByInstanceId(instanceId);
        if(instance == null){
            instance = new Instance();
            instance.setInstanceId(instanceId);
            instance.setNamespace(namespace);
            instance.setStatus(status);
            instance.setUpdateTime(instance.getCreateTime());
            int count = InstanceDao.insert(instance);
            if(count <= 0){
                throw new BizException("instanceId = " + instanceId + " 当前实例初始化失败");
            }
        }

        if(instance.getStatus() == TimerStatus.RUNNING.getValue()){
            setToRunning();
        }else{
            jobManager.pauseInstance();
            setToPausing();
        }
        isInitFinished = true;
    }

    /**
     * 当前实例是否已完成初始化
     * @return
     */
    public boolean isInitFinished(){
        return isInitFinished;
    }

    /**
     * 判断当前实例所在的namespace，是否全部实例都处于暂停中
     */
    public boolean isAllInstancePausing(String namespace){
        if(StringUtil.isEmpty(namespace)){
            throw new BizException("namespace不能为空");
        }

        List<Instance> instanceList = InstanceDao.listByNamespace(namespace);
        boolean isPausing = true;
        for(Instance stage : instanceList){
            if(stage.getStatus() != TimerStatus.STAND_BY.getValue()){
                isPausing = false;
                break;
            }
        }
        return isPausing;
    }

    /**
     * 判断当前实例所在的namespace，是否全部实例都处于运行
     */
    public boolean isAllInstanceRunning(String namespace){
        if(StringUtil.isEmpty(namespace)){
            throw new BizException("namespace不能为空");
        }

        List<Instance> instanceList = InstanceDao.listByNamespace(namespace);
        boolean isRunning = true;
        for(Instance stage : instanceList){
            if(stage.getStatus() != TimerStatus.RUNNING.getValue()){
                isRunning = false;
                break;
            }
        }
        return isRunning;
    }

    /**
     * 暂停当前实例：需要确保事务的一致性
     * @param namespace
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean pause(String namespace){
        if(isRunning == false){
            return true;
        }

        boolean isOk = InstanceDao.updateInstanceStatus(TimerStatus.STAND_BY.getValue(), getInstanceId(), namespace);
        if(isOk){
             if(! jobManager.isStandByMode()){
                 jobManager.pauseInstance();
             }
            logger.info("从namespace中同步当前实例的状态为: 挂起中");
            setToPausing();
        }
        return isOk;
    }

    /**
     * 恢复当前实例：需要确保事务的一致性
     * @param namespace
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean resume(String namespace){
        if(isRunning){
            return true;
        }

        boolean isOk = InstanceDao.updateInstanceStatus(TimerStatus.RUNNING.getValue(), getInstanceId(), namespace);
        if(isOk){
            if(jobManager.isStandByMode()){
                jobManager.resumeInstance();
            }
            logger.info("从namespace中同步当前实例的状态为: 运行中");
            setToRunning();
        }
        return isOk;
    }

    public PageResult<List<Instance>> listInstancePage(Map<String, Object> paramMap, PageParam pageParam){
        PageResult<List<Instance>> pageResult = InstanceDao.listPage(paramMap, pageParam);
        if(pageResult.getData() != null && pageResult.getData().size() > 0){
            //一般情况下namespace也就只有几个
            PageResult<Map<String, Namespace>> result = namespaceDao.mapByIdPage(null, PageParam.newInstance(1, 200));
            Map<String, Namespace> namespaceMap = result.getData();
            for(Instance instance : pageResult.getData()){
                if(namespaceMap != null && namespaceMap.containsKey(instance.getNamespace())){
                    instance.setNamespaceStatus(namespaceMap.get(instance.getNamespace()).getStatus());
                }
            }
        }
        return pageResult;
    }

    public String getInstanceId(){
        return IPUtil.getFirstLocalIp();
    }

    private synchronized void setToRunning(){
        isRunning = true;
    }
    private synchronized void setToPausing(){
        isRunning = false;
    }
}
