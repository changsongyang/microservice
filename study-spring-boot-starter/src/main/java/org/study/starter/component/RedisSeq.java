package org.study.starter.component;

import org.study.common.util.utils.DateUtil;

import java.util.*;

public class RedisSeq {
    public static final int REDIS_LOOP_ID_MAX_VALUE = 999999999;
    public static final String REDIS_LOOP_ID_FORMAT = "%09d";

    private RedisClient redisClient;

    public RedisSeq(RedisClient redisClient){
        this.redisClient = redisClient;
    }

    /**
     * 使用redis获取下一个Id
     * @param key   redis缓存的key
     * @return
     */
    public Long nextId(String key){
        return redisClient.incr(key);
    }

    /**
     * 使用redis获取单个循环Id，当Id序列号超过10亿时，则重新从1开始
     * @param key
     * @return
     */
    public Long nextLoopId(String key){
        return nextLoopId(key, 1, REDIS_LOOP_ID_MAX_VALUE);
    }

    /**
     * 使用redis批量获取循环Id，当Id序列号超过10亿时，则重新从1开始
     * @param key
     * @param idCount
     * @return
     */
    public List<Long> nextLoopId(String key, int idCount){
        long maxId = nextLoopId(key, idCount, REDIS_LOOP_ID_MAX_VALUE);
        List<Long> idList = new ArrayList<>(idCount);
        for(long index=maxId; index > 0; index--){
            idList.add(index);
        }
        return idList;
    }

    /**
     * 使用redis获取循环Id，并按照： 前缀 + 6位年月日 + 10位Id序列号  的格式来返回流水号，当Id序列号超过10亿时，则重新从1开始
     * @param key
     * @return
     */
    public String nextLoopId(String key, String prefix){
        long seqValue = nextLoopId(key, 1, REDIS_LOOP_ID_MAX_VALUE);
        return prefix + DateUtil.formatShortDate(new Date()) + String.format(REDIS_LOOP_ID_FORMAT, seqValue);
    }

    /**
     * 使用redis批量获取循环Id，并按照： 前缀 + 6位年月日 + 10位Id序列号  的格式来返回流水号，当Id序列号超过10亿时，则重新从1开始
     * @param key
     * @param idCount
     * @return
     */
    public List<String> nextLoopId(String key, String prefix, int idCount){
        long maxId = nextLoopId(key, idCount, REDIS_LOOP_ID_MAX_VALUE);
        List<String> idList = new ArrayList<>(idCount);
        prefix = prefix + DateUtil.formatShortDate(new Date());
        for(long index=maxId; index > 0; index--){
            idList.add(prefix + String.format(REDIS_LOOP_ID_FORMAT, index));
        }
        return idList;
    }

    private long nextLoopId(String key, int idCount, long maxValue){
        if(idCount <= 0 || idCount > maxValue){
            throw new RuntimeException("增长步数须大于0且不能超过最大值,idCount="+idCount+",maxValue="+maxValue);
        }else if(maxValue > REDIS_LOOP_ID_MAX_VALUE){
            throw new RuntimeException("最大ID不能超过: " + REDIS_LOOP_ID_MAX_VALUE);
        }

        long seqValue;
        if(idCount == 1){
            seqValue = nextId(key);
        }else{
            seqValue = redisClient.incrBy(key, idCount);
        }

        if (seqValue > maxValue) {
            return redisClient.resetLoopNum(key, idCount, maxValue);
        } else {
            return seqValue;
        }
    }
}
