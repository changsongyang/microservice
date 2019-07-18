package org.study.starter.utils;

import com.google.common.base.CaseFormat;

public class SnakeCaseUtil {
    public static String toSnakeCase(String fieldName, boolean toLower){
        if(isUnderscore(fieldName)){
            return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, fieldName);
        }else if(isCamel(fieldName)){
            return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, fieldName);
        }else{
            return toLower ? fieldName.toLowerCase() : fieldName.toUpperCase();
        }
    }

    public static boolean isUnderscore(String field){
        return field.contains("_");
    }

    public static boolean isCamel(String field){
        return ! (field.equals(field.toUpperCase()) || field.equals(field.toLowerCase()));
    }

    public static void main(String[] args){
        long start = System.currentTimeMillis();
        for(int i=0; i<100000; i++){
            toSnakeCase("adsdsddsdsdBALANCE", true);
        }
        System.out.println("mills time="+ (System.currentTimeMillis()-start));
    }
}
