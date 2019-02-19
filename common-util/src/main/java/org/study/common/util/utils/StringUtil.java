package org.study.common.util.utils;

import java.util.UUID;

/**
 * Created by jo on 2017/8/12.
 */
public class StringUtil{
    public static boolean isEmpty(Object value){
        return value == null || value.toString().trim().length() == 0;
    }

    public static boolean isNotEmpty(Object value){
        return !isEmpty(value);
    }

    public static String getUUIDStr(){
        return UUID.randomUUID().toString();
    }

    public static String getMD5UUIDStr(){
        return MD5Util.getMD5Hex(UUID.randomUUID().toString());
    }
}
