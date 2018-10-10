package org.study.common.util.utils;

import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.config.Config;

/**
 * @Title：操作redis的类,需要JDK1.8以上才能使用
 *
 * @Description：RedissonClient是redis的java客户端，跟redis连接只是其最基本的功能，并且还提供自动重连的机制，除此之外，
 *      它还提供多种分布式对象，特别适合在分布式环境下使用，RSet、RList、RMap、RLock等等对象的使用方法，完全跟java原生的
 *      Set、List、Lock的使用方法一样，因为Redisson提供的对象都是有重写父类中的方法的，都是能够支持分布式操作的，而对于分布式锁
 *      {@link org.redisson.RedissonLock}，如果客户端连接断开，则会自动释放锁，更多资料可以查看：https://github.com/redisson/redisson/wiki
 *
 * @Author： chenyf
 * @Version： V1.0
 * @Date： 2018/4/17 11:26
 */
public class RedissonUtil {
    /**
     * 单例模式
     * @param address   地址：如 127.0.0.1:6768
     * @param password
     * @return
     */
    public static RedissonClient singleInstance(String address, String password){
        Config config = new Config();
        config.useSingleServer()
                .setAddress(address)
                .setPassword(password);
        return Redisson.create(config);
    }

    /**
     * 主从模式
     * @param masterAddress
     * @param masterPassword
     * @param slaveAddress
     * @return
     */
    public static RedissonClient masterSlaveInstance(String masterAddress, String masterPassword, String slaveAddress){
        Config config = new Config();
        String[] slaveAddressArr = slaveAddress.split(",");
        config.useMasterSlaveServers()
                .setMasterAddress(masterAddress)
                .setPassword(masterPassword)
                .addSlaveAddress(slaveAddressArr);
        return Redisson.create(config);
    }

    /**
     * 哨兵模式
     * @param masterName
     * @param sentinelAddress
     * @return
     */
    public static RedissonClient sentinelInstance(String masterName, String sentinelAddress){
        Config config = new Config();
        String[] sentinelAddressArr = sentinelAddress.split(",");
        config.useSentinelServers()
                .setMasterName(masterName)
                .addSentinelAddress(sentinelAddressArr);
        return Redisson.create(config);
    }

    /**
     * 自定义配置
     * @param config
     * @return
     */
    public static RedissonClient getInstance(Config config){
        return Redisson.create(config);
    }
}
