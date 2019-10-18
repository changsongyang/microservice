package org.study.starter.component;

import com.tpay.common.util.utils.JsonUtil;
import com.tpay.starter.enums.RedisMode;
import org.springframework.util.Assert;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.util.Pool;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RedisClient {
    private static final int DEFAULT_CACHE_SECONDS = 60 * 1;// 单位秒 设置成一分钟
    private RedisMode redisMode;
    private boolean isClusterMode;
    private JedisCluster jedisCluster;
    private Pool<Jedis> pool;

    public RedisClient(RedisMode redisMode, Pool<Jedis> pool, JedisCluster jedisCluster){
        Assert.notNull(redisMode, "redisMode is null");

        this.redisMode = redisMode;
        if(RedisMode.CLUSTER.equals(redisMode)){
            Assert.notNull(jedisCluster, "When redisMode is CLUSTER, jedisCluster can not be null");
            this.jedisCluster = jedisCluster;
            this.isClusterMode = true;
        }else{
            Assert.notNull(pool, "pool can not be null");
            this.pool = pool;
            this.isClusterMode = false;
        }
    }

    public String get(String key){
        if(isClusterMode){
            return jedisCluster.get(key);
        }else{
            Jedis jedis = null;
            try{
                jedis = pool.getResource();
                return jedis.get(key);
            } finally {
                if(jedis != null){
                    jedis.close();
                }
            }
        }
    }

    public <T> T get(String key, Class<T> clz){
        if(isClusterMode){
            String value = jedisCluster.get(key);
            if(value != null){
                return JsonUtil.toBean(value, clz);
            }else{
                return null;
            }
        }else{
            Jedis jedis = null;
            try{
                jedis = pool.getResource();
                String value = jedis.get(key);
                if(value != null){
                    return JsonUtil.toBean(value, clz);
                }else{
                    return null;
                }
            } finally {
                if(jedis != null){
                    jedis.close();
                }
            }
        }
    }

    public boolean set(String key, String value){
        if(isClusterMode){
            jedisCluster.set(key, value);
            return true;
        }else{
            Jedis jedis = null;
            try{
                jedis = pool.getResource();
                jedis.set(key, value);
                return true;
            } finally {
                if(jedis != null){
                    jedis.close();
                }
            }
        }
    }

    public <T> boolean set(String key, T value, int seconds){
        if (seconds == 0) {// 如果超时时间为0，则设置为默认一分钟
            seconds = DEFAULT_CACHE_SECONDS;
        }

        boolean isNoExpire = seconds <= -1;//-1 表示用不超时
        if(isClusterMode){
            if(isNoExpire){
                jedisCluster.set(key, JsonUtil.toString(value));
            }else{
                jedisCluster.setex(key, seconds, JsonUtil.toString(value));
            }
            return true;
        }else{
            Jedis jedis = null;
            try{
                jedis = pool.getResource();
                if(isNoExpire){
                    jedis.set(key, JsonUtil.toString(value));
                }else{
                    jedis.setex(key, seconds, JsonUtil.toString(value));
                }
                return true;
            } finally {
                if(jedis != null){
                    jedis.close();
                }
            }
        }
    }

    public String hget(String key, String field){
        if(isClusterMode){
            return jedisCluster.hget(key, field);
        }else{
            Jedis jedis = null;
            try{
                jedis = pool.getResource();
                return jedis.hget(key, field);
            } finally {
                if(jedis != null){
                    jedis.close();
                }
            }
        }
    }

    public boolean hset(String key, String field, String value){
        if(isClusterMode){
            return jedisCluster.hset(key, field, value) >= 0;
        }else{
            Jedis jedis = null;
            try{
                jedis = pool.getResource();
                return jedis.hset(key, field, value) >= 0;
            } finally {
                if(jedis != null){
                    jedis.close();
                }
            }
        }
    }

    public Long hincr(String key, String field){
        if(isClusterMode){
            return jedisCluster.hincrBy(key, field, 1);
        }else{
            Jedis jedis = null;
            try{
                jedis = pool.getResource();
                return jedis.hincrBy(key, field, 1);
            } finally {
                if(jedis != null){
                    jedis.close();
                }
            }
        }
    }

    public Long incr(String key){
        if(isClusterMode){
            return jedisCluster.incr(key);
        }else{
            Jedis jedis = null;
            try{
                jedis = pool.getResource();
                return jedis.incr(key);
            } finally {
                if(jedis != null){
                    jedis.close();
                }
            }
        }
    }

    public Long incrBy(String key, int increment){
        if(isClusterMode){
            return jedisCluster.incrBy(key, increment);
        }else{
            Jedis jedis = null;
            try{
                jedis = pool.getResource();
                return jedis.incrBy(key, increment);
            } finally {
                if(jedis != null){
                    jedis.close();
                }
            }
        }
    }

    public <T> T eval(String script, List<String> keys, String... params){
        if(isClusterMode){
            return (T) jedisCluster.eval(script, keys, Arrays.asList(params));
        }else{
            Jedis jedis = null;
            try{
                jedis = pool.getResource();
                return (T) jedis.eval(script, keys, Arrays.asList(params));
            } finally {
                if(jedis != null){
                    jedis.close();
                }
            }
        }
    }

    public Long resetLoopNum(String key, int incrStep, long maxValue){
        String script = "local currValue = redis.call('incrby', KEYS[1], ARGV[1]); "
                + "if currValue > tonumber(ARGV[2]) then "
                + "redis.call('set', KEYS[1], 0); "
                + "return redis.call('incrby', KEYS[1], ARGV[1]) "
                + "else "
                + "return currValue "
                + "end";
        Long id = eval(script, Collections.singletonList(key), String.valueOf(incrStep), String.valueOf(maxValue));
        return id;
    }

    public void destroy(){
        if(jedisCluster != null){
            jedisCluster.close();
        }
        if(pool != null){
            pool.destroy();
        }
    }
}
