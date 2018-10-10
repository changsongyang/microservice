package org.study.common.util.utils;

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
}
