package org.study.api.gateway.ratelimit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.List;

/**
 * @description 简易限流器
 * @author chenyf
 * @date 2019-02-23
 */
public class SimpleRateLimiter {
    private RedisTemplate<String, String> redisTemplate;
    private RedisScript<Long> script;

    public SimpleRateLimiter(RedisTemplate<String, String> redisTemplate,
                             RedisScript<Long> script) {
        this.redisTemplate = redisTemplate;
        this.script = script;
    }

    public boolean isAllow(String key, long limit, long expireMills){
//        List<String> args = Arrays.asList(String.valueOf(limit), String.valueOf(expireMills));
        List<String> keys = Collections.singletonList(key);
        Long result = this.redisTemplate.execute(this.script, keys, String.valueOf(limit), String.valueOf(expireMills));
        return result != null && result == 1;
    }
}
