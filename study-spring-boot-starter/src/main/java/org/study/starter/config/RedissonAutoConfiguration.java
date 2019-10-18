package org.study.starter.config;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.study.common.statics.exceptions.BizException;
import org.study.starter.component.RedisLock;
import org.study.starter.enums.RedisClusterMode;
import org.study.starter.properties.RedissonProperties;

import java.util.Arrays;

@ConditionalOnClass(RedissonClient.class)
@EnableConfigurationProperties(RedissonProperties.class)
@Configuration
public class RedissonAutoConfiguration {

    @ConditionalOnProperty(prefix = "redis", value = {"clusterMode", "urls"})
    @Bean
    public RedisLock redisClient(RedissonProperties redissonProperties){
        if(RedisClusterMode.SINGLE.equals(redissonProperties.getClusterMode())){
            return RedisLock.Builder.singleMode(redissonProperties.getUrls(), redissonProperties.getPassword());
        }else if(RedisClusterMode.SENTINEL.equals(redissonProperties.getClusterMode())){
            String[] addressArr = redissonProperties.getUrls().split(",");
            return RedisLock.Builder.sentinelMode(redissonProperties.getMasterName(), redissonProperties.getPassword(), Arrays.asList(addressArr));
        }else if(RedisClusterMode.REDIS_CLUSTER.equals(redissonProperties.getClusterMode())){
            String[] addressArr = redissonProperties.getUrls().split(",");
            return RedisLock.Builder.clusterMode(Arrays.asList(addressArr), redissonProperties.getPassword());
        }else{
            throw new BizException("Not supported RedisClusterMode: " + redissonProperties.getClusterMode());
        }
    }
}
