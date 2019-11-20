package com.xpay.starter.comp.config;

import com.xpay.starter.comp.component.RedisLock;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisOperations;

@ConditionalOnClass({Redisson.class, RedisOperations.class})
@Configuration
public class RedisLockAutoConfiguration {

//    @ConditionalOnProperty(name = "com.xpay.redis-lock.enabled", havingValue = "true")
    @Bean(destroyMethod = "destroy")
    public RedisLock redisLock(RedissonClient redisson){
        return new RedisLock(redisson);
    }
}
