package org.study.service.timer.biz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.study.common.statics.exceptions.BizException;
import org.study.common.util.utils.IPUtil;
import org.study.service.timer.dao.InstanceStageDao;
import org.study.service.timer.job.base.JobManager;
import org.study.facade.timer.entity.InstanceStage;

import java.util.Date;

/**
 * 实例状态逻辑层
 */
@Component
public class InstanceStageBiz {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private boolean isInitFinished = false;
    private boolean isRunning = true;

    @Autowired
    JobManager jobManager;
    @Autowired
    InstanceStageDao instanceStageDao;

    @Transactional(rollbackFor = Exception.class)
    public void pauseInstance(){
        synchronized (InstanceStageBiz.class){
            if(! isInitFinished()){
                throw new BizException("未完成初始化！");
            }else if(this.isStandByMode()){
                return;
            }
            
            String instanceId = IPUtil.getFirstLocalIp();
            logger.info("instanceId = {}", instanceId);
            InstanceStage stage = instanceStageDao.getByInstanceId(instanceId);
            boolean isSuccess = true;
            if(stage == null){
                stage = new InstanceStage();
                stage.setInstanceId(instanceId);
                stage.setStatus(InstanceStage.STAND_BY_STAGE);
                stage.setUpdateTime(stage.getCreateTime());
                isSuccess = instanceStageDao.insert(stage) > 0;
            }else if(stage.getStatus() != InstanceStage.STAND_BY_STAGE){
                stage.setStatus(InstanceStage.STAND_BY_STAGE);
                stage.setUpdateTime(new Date());
                isSuccess = instanceStageDao.update(stage) > 0;
            }

            if(! isSuccess){
                throw new BizException("暂停失败！数据库处理失败！");
            }

            jobManager.pauseInstance();
            isRunning = false;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void resumeInstance(){
        synchronized (InstanceStageBiz.class){
            if(! isInitFinished()){
                throw new BizException("未完成初始化！");
            }else if(! this.isStandByMode()){
                return;
            }

            String instanceId = IPUtil.getFirstLocalIp();
            logger.info("instanceId = {}", instanceId);
            InstanceStage stage = instanceStageDao.getByInstanceId(instanceId);
            if(stage != null && stage.getStatus() != InstanceStage.RUNNING_STAGE){
                stage.setStatus(InstanceStage.RUNNING_STAGE);
                stage.setUpdateTime(new Date());

                boolean isSuccess = instanceStageDao.update(stage) > 0;
                if(! isSuccess){
                    throw new BizException("恢复失败，数据库更新失败！");
                }
            }

            jobManager.resumeInstance();
            isRunning = true;
        }
    }

    /**
     * 断言实例是暂停中，如果当前实例为非暂停中，则会抛出异常
     */
    public void assertPausing(){
        if(isRunning){
            throw new BizException("当前实例非暂停中");
        }
    }

    /**
     * 断言实例是运行中，如果当前实例为暂停中，则会抛出异常
     */
    public void assertRunning(){
        if(! isRunning){
            throw new BizException("当前实例非'运行中'");
        }
    }

    /**
     * 当前实例是否已完成初始化
     * @return
     */
    public boolean isInitFinished(){
        return isInitFinished;
    }

    /**
     * 当前实例是否处于挂起状态
     * @return
     */
    public boolean isStandByMode(){
        return ! isRunning;
    }

    /**
     * 初始化
     */
    public void initialize(){
        String instanceId = IPUtil.getFirstLocalIp();

        synchronized (InstanceStageBiz.class){
            InstanceStage stage = instanceStageDao.getByInstanceId(instanceId);
            if(stage == null){
                isRunning = true;
            }else if(stage.getStatus() == InstanceStage.RUNNING_STAGE){
                isRunning = true;
            }else if(stage.getStatus() == InstanceStage.STAND_BY_STAGE){
                isRunning = false;
            }
            if(isRunning == false){
                jobManager.pauseInstance();
            }
            isInitFinished = true;
        }
    }
}
