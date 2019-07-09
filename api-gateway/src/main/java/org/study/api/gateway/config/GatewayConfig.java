package org.study.api.gateway.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.validation.Validator;
import org.study.api.gateway.filters.gateway.IPValidGatewayFilterFactory;
import org.study.api.gateway.filters.gateway.RateLimiterGatewayFilterFactory;
import org.study.api.gateway.ratelimit.PathKeyResolver;
import org.study.api.gateway.ratelimit.PathRedisRateLimiter;
import org.study.api.gateway.ratelimit.SimpleRateLimiter;
import org.study.api.gateway.service.ValidFailServiceImpl;
import org.study.common.api.helpers.RequestHelper;
import org.study.common.api.service.UserService;
import org.study.common.api.service.ValidFailService;
import org.study.common.api.service.impl.UserServiceImpl;
import org.study.common.api.vo.MerchantInfo;

import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootConfiguration
public class GatewayConfig {
    @Value("${study.api.ip-black-list-pattern:\"\"}")
    private String ipBlackListPattern;

    @Bean
    public IPValidGatewayFilterFactory ipValidGatewayFilterFactory(){
        return new IPValidGatewayFilterFactory();
    }

    @Primary
    @Bean
    public PathRedisRateLimiter pathRedisRateLimiter(ReactiveRedisTemplate<String, String> redisTemplate,
                                                     @Qualifier(PathRedisRateLimiter.REDIS_SCRIPT_NAME) RedisScript<List<Long>> redisScript,
                                                     Validator validator) {
        return new PathRedisRateLimiter(redisTemplate, redisScript, validator);
    }

    @Bean
    public RateLimiterGatewayFilterFactory rateLimiterGatewayFilterFactory(PathRedisRateLimiter pathRedisRateLimiter, PathKeyResolver pathKeyResolver) {
        return new RateLimiterGatewayFilterFactory(pathRedisRateLimiter, pathKeyResolver);
    }

    @Bean("pathKeyResolver")
    public PathKeyResolver pathKeyResolver(){
        return new PathKeyResolver();
    }

    @Bean
    public SimpleRateLimiter simpleRateLimiter(RedisTemplate<String, String> redisTemplate, RedisScript<Long> simpleRateLimiterScript){
        return new SimpleRateLimiter(redisTemplate, simpleRateLimiterScript);
    }

    @Bean("simpleRateLimiterScript")
    public RedisScript simpleRateLimiterScript() {
        DefaultRedisScript redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/simple_rate_limiter.lua")));
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    @Bean
    public ValidFailService validFailService(){
        return new ValidFailServiceImpl();
    }

    @Bean
    public RequestHelper requestHelper(UserService userService){
        return new RequestHelper(userService);
    }

    /**
     * guava 本地缓存
     * @return
     */
    @Bean
    public Cache<String, MerchantInfo> cache() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(60, TimeUnit.SECONDS)//过期时间
                .maximumSize(10000)
                .initialCapacity(50)
                .concurrencyLevel(10)
                .build();
    }

    @Bean
    public UserService userService(){
        return new UserServiceImpl(cache());
    }

    public String getIpBlackListPattern() {
        return ipBlackListPattern;
    }

    public void setIpBlackListPattern(String ipBlackListPattern) {
        this.ipBlackListPattern = ipBlackListPattern;
    }
}
