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

    public static boolean isLengthOver(String str, int maxLen){
        if(str == null){
            return false;
        }else{
            return str.length() > maxLen;
        }
    }

    public static boolean isLengthOk(String str, int minLen, int maxLen){
        if(str == null){
            return false;
        }else{
            return minLen <= str.length() && str.length() <= maxLen;
        }
    }

    public static String getUUIDStr(){
        return UUID.randomUUID().toString();
    }

    public static String getMD5UUIDStr(){
        return MD5Util.getMD5Hex(UUID.randomUUID().toString());
    }

    public static void main(String[] args){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1024; i += 10) {
            sb.append("hello baby");
        }
        System.out.println(sb);
    }
}
