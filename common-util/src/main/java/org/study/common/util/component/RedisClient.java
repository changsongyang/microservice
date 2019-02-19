package org.study.common.util.component;

import org.redisson.Redisson;
import org.redisson.RedissonRedLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.study.common.statics.exceptions.BizException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 操作redis的客户端
 */
public class RedisClient {
    public final static String SEPARATOR = ",";
    private RedissonClient client;

    @PostConstruct
    public void init(){
        if(client == null){
            throw new BizException("RedissonClient 不能为null");
        }
    }

    @PreDestroy
    public void destroy(){
        if(client != null){
            client.shutdown();
        }
    }

    public RedissonClient getClient(){
        return client;
    }

    /**
     * 单机模式
     * @param address   地址：如 127.0.0.1:6768
     * @param password
     * @return
     */
    public void singleMode(String address, String password){
        Config config = new Config();
        config.useSingleServer()
                .setAddress(address)
                .setPassword(password);
        this.client = Redisson.create(config);
    }

    /**
     * 主从模式
     * @param masterUrl
     * @param masterPass
     * @param slaveUrls
     * @return
     */
    public void masterSlaveMode(String masterUrl, String masterPass, String slaveUrls){
        Config config = new Config();
        String[] slaveAddressArr = slaveUrls.split(SEPARATOR);
        config.useMasterSlaveServers()
                .setMasterAddress(masterUrl)
                .setPassword(masterPass)
                .addSlaveAddress(slaveAddressArr);
        this.client = Redisson.create(config);
    }

    /**
     * 哨兵模式
     * @param masterName
     * @param sentinelUrls
     * @return
     */
    public void sentinelMode(String masterName, String sentinelUrls){
        Config config = new Config();
        String[] sentinelAddressArr = sentinelUrls.split(SEPARATOR);
        config.useSentinelServers()
                .setMasterName(masterName)
                .addSentinelAddress(sentinelAddressArr);
        this.client = Redisson.create(config);
    }

    /**
     * 自定义配置
     * @param config
     * @return
     */
    public void newInstance(Config config){
        this.client = Redisson.create(config);
    }
}
