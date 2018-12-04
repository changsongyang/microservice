package org.study.common.util.utils;

import java.util.Random;

/**
 * @Description:
 * @author: chenyf
 * @Date: 2018/2/7
 */
public class RandomUtil {

    private static int getInt(int min, int max){
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    public static void main(String[] args){
        for(int i=0; i<100; i++){
            System.out.println("i=" + i + " random=" + getInt(-100, 100));
        }
    }
}
