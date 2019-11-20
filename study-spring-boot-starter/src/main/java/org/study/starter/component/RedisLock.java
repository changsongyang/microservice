package org.study.starter.component;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.study.common.util.utils.JsonUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * redis分布式锁
 */
public class RedisLock {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private String lockNamePrefix = "lock-";
    private RedissonClient client;
    private ConcurrentHashMap<String, RLock> unlockWhenShutdownMap = new ConcurrentHashMap<>();

    public RedisLock(RedissonClient client){
        if(client == null){
            throw new RuntimeException("RedissonClient 不能为null");
        }
        this.client = client;
    }

    public String getLockNamePrefix() {
        return lockNamePrefix;
    }

    public void setLockNamePrefix(String lockNamePrefix) {
        this.lockNamePrefix = lockNamePrefix;
    }

    public void destroy(){
        if(client != null){
            try{
                //waiting for dubbo shutdown
                Thread.sleep(2000);
            }catch(Exception e){}

            for(Map.Entry<String, RLock> entry : unlockWhenShutdownMap.entrySet()){
                logger.info("lockName={} 应用关闭前强制释放锁", entry.getKey());
                forceUnlock(entry.getValue());
            }
            client.shutdown(3, 7, TimeUnit.SECONDS);
        }
    }

    public RedissonClient getClient(){
        return client;
    }

    /**
     * 获取分布式锁，有如下注意事项：
     *     1、请使用当前类中的相关方法来释放锁，而不要直接调用RLock的unlock或者forceUnlock方法来释放锁，因为在{@link #unlockWhenShutdownMap}有缓存，一直不清掉的话可能会造成内存泄露
     *
     * @param lockName             锁名称
     * @param waitMills            获取锁的等待时间
     * @param expireMills          锁的有效时间
     * @param unlockWhenShutdown   是否需要在应用关闭时强行释放锁
     * @return
     */
    public RLock tryLock(String lockName, int waitMills, long expireMills, boolean unlockWhenShutdown){
        Set<String> set = new HashSet<>(1);
        set.add(lockName);
        List<RLock> lockList = tryLock(set, waitMills, expireMills, unlockWhenShutdown);
        if(lockList != null && lockList.size() > 0){
            return lockList.get(0);
        }else {
            return null;
        }
    }

    /**
     * 批量获取锁，要么全部获取成功，要么全部获取失败
     * @param lockNameList          锁名称
     * @param waitMills             获取锁的等待时间
     * @param expireMills           锁的有效时间
     * @param unlockWhenShutdown    是否需要在应用关闭时强行释放锁
     * @return
     */
    public List<RLock> tryLock(Set<String> lockNameList, int waitMills, long expireMills, boolean unlockWhenShutdown){
        List<RLock> lockList = new ArrayList<>(lockNameList.size());
        TimeUnit unit = TimeUnit.MILLISECONDS;
        //开始时间
        long startTime = System.currentTimeMillis();
        //剩余时间
        long remainTime = waitMills;
        //获取锁时的真正等待时间
        long awaitTime = 0;

        for(String lockName : lockNameList){
            try{
                lockName = getRealLockName(lockName);
                RLock lock = getClient().getLock(lockName);
                remainTime -= System.currentTimeMillis() - startTime;
                startTime = System.currentTimeMillis();
                awaitTime = Math.max(remainTime, 0);

                if(awaitTime <= 0){
                    logger.error("lockName={} lockNameList = {} 获取锁超时", lockName, JsonUtil.toString(lockNameList));
                    break;
                }else if(lock.tryLock(awaitTime, expireMills, unit)){
                    lockList.add(lock);
                    if(unlockWhenShutdown){
                        addToShutdownMap(lock);
                    }
                }else{
                    logger.error("lockName={} 获取锁失败", lockName);
                    break;
                }
            }catch(Throwable e){
                logger.error("lockName={} 获取锁时出现异常", lockName, e);
                break;
            }
        }

        //如果其中有任何一个账户获取锁失败，则全部锁释放
        if(lockList.size() != lockNameList.size()){
            unlock(lockList);
            //返回空List
            return new ArrayList<>();
        }
        return lockList;
    }

    /**
     * 释放锁，释放锁的线程和加锁的线程必须是同一个才行，如果需要强行释放锁，请查看 {@link #forceUnlock(RLock)}
     * @param lockList
     * @return 如果全部解锁成功，则返回true，否则，返回false
     */
    public void unlock(List<RLock> lockList){
        if(lockList == null || lockList.isEmpty()){
            return;
        }

        for(RLock lock : lockList){
            try{
                unlock(lock);
            }catch(Throwable t){
                logger.error("释放锁异常", t);
            }
        }
    }

    /**
     * 释放锁，释放锁的线程和加锁的线程必须是同一个才行，如果需要强行释放锁，请查看 {@link #forceUnlock(RLock)}
     * @param lock
     * @return
     */
    public void unlock(RLock lock) throws RuntimeException {
        try{
            lock.unlock();
            removeFromShutdownMap(lock.getName());
        }catch(Throwable t){
            throw new RuntimeException("lockName = "+lock.getName()+" 释放锁时出现异常", t);
        }
    }

    /**
     * 强行释放锁，不管释放锁的线程是不是跟加锁时的线程一样，都可以释放锁
     * @param lock
     * @return
     */
    public void forceUnlock(RLock lock){
        try{
            removeFromShutdownMap(lock.getName());
            lock.forceUnlock();
        }catch(Throwable t){
            logger.error("lockName = {} 强制释放锁时出现异常", lock.getName(), t);
        }
    }

    private String getRealLockName(String lockName){
        return getLockNamePrefix() + lockName;
    }
    private void addToShutdownMap(RLock lock){
        unlockWhenShutdownMap.putIfAbsent(lock.getName(), lock);
    }
    private void removeFromShutdownMap(String lockName){
        unlockWhenShutdownMap.remove(lockName);
    }
}
