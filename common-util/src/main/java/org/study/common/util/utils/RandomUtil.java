package org.study.common.util.utils;

import java.util.Random;
import java.util.UUID;

/**
 * @Description:
 * @author: chenyf
 * @Date: 2018/2/7
 */
public class RandomUtil {
    public static String get16LenStr(){
        if(getInt(2) == 0){
            return MD5Util.getMD5Hex(UUID.randomUUID().toString()).substring(16);
        }else{
            return MD5Util.getMD5Hex(UUID.randomUUID().toString()).substring(1, 17);
        }
    }

    public static String get32LenStr(){
        return MD5Util.getMD5Hex(UUID.randomUUID().toString());
    }

    private static int getInt(int min, int max){
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    public static int getInt(int max){
        Random random = new Random();
        return random.nextInt(max);
    }
}
