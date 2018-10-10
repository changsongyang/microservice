package org.study.common.util.utils;

import java.util.UUID;

/**
 * @Description:
 * @author: chenyf
 * @Date: 2018/2/7
 */
public class RandomUtil {

    public static String getUUIDStr(){
        return UUID.randomUUID().toString();
    }

    public static String getMD5UUIDStr(){
        return MD5Util.getMD5Str(UUID.randomUUID().toString());
    }
}
