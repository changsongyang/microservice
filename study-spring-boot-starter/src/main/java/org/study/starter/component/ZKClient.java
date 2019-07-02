package org.study.starter.component;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.retry.RetryNTimes;
import org.study.common.statics.exceptions.BizException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 操作zookeeper的客户端
 */
public class ZKClient {
    private CuratorFramework client;
    private String lockRootPath = "/lock";
    private String urls;

    public void setUrls(String urls) {
        this.urls = urls;
    }

    @PostConstruct
    public void init(){
        if(urls == null || urls.trim().length() == 0){
            throw new BizException("urls不能为空");
        }
        client = CuratorFrameworkFactory.newClient(urls, new RetryNTimes(3, 1000));
        client.start();
    }

    @PreDestroy
    public void destroy(){
        if(client != null){
            client.close();
        }
    }

    /**
     * 可重入锁，InterProcessMutex的实例对象是可重用的，外部可看情况是否需要缓存起来重用
     * @param path
     * @return
     */
    public InterProcessMutex getReentrantLock(String path){
        if(! path.startsWith("/")){
            path = "/" + path;
        }
        return new InterProcessMutex(getCuratorClient(), lockRootPath + path);
    }

    /**
     * 共享锁，不可重入
     * @param path
     * @return
     */
    public InterProcessSemaphoreMutex getShareLock(String path){
        if(! path.startsWith("/")){
            path = "/" + path;
        }
        return new InterProcessSemaphoreMutex(getCuratorClient(), lockRootPath + path);
    }

    public CuratorFramework getCuratorClient(){
        return client;
    }
}
