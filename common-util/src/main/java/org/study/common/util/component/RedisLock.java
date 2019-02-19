package org.study.common.util.component;

import org.redisson.RedissonLock;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RedisLock {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ConcurrentHashMap<Integer, RedissonClient> clientMap = new ConcurrentHashMap();
    private ConcurrentHashMap<String, RedissonRedLock> redLockMap = new ConcurrentHashMap();
    private Lock lock = new ReentrantLock();

    public synchronized void addClient(RedissonClient client){
        int index = clientMap.size() == 0 ? 0 : clientMap.size() - 1;
        clientMap.putIfAbsent(index, client);
    }

    /**
     * 获取锁
     * @param name
     * @return
     */
    public RedissonLock getLock(String name){
        int hash = name.hashCode();
        if(hash < 0){
            hash = hash * (-1);
        }

        int index = hash % clientMap.size();
        return (RedissonLock) clientMap.get(index).getLock(name);
    }

    /**
     * 获取RedLock TODO 需要搞明白 RedissonRedLock 对象是否可以缓存起来重用
     * @param name
     * @return
     * {@link #getRedLock(String)}
     */
    public RedissonRedLock getRedLock(String name){
        if(clientMap.size() < 3){
            throw new RuntimeException("使用redLock时，必须要有三个以上的redis集群");
        }

        if(! redLockMap.containsKey(name)){
            RLock[] locks = new RLock[clientMap.size()];
            for(Map.Entry<Integer, RedissonClient> entry : clientMap.entrySet()){
                locks[entry.getKey()] = entry.getValue().getLock(name);
            }
            RedissonRedLock redLock = new RedissonRedLock(locks);
            redLockMap.putIfAbsent(name, redLock);

//            lock.lock();
//
//            try {
//                if(! redLockMap.containsKey(name)){
//
//                }
//            } finally {
//                lock.unlock();
//            }
        }
        return redLockMap.get(name);
    }
}
