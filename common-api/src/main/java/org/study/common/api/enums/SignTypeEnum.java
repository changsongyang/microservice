package com.gw.api.base.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 签名类型：为做到兼容性，value的值需要跟 com.gw.facade.trade.enums.OrderEncryptTypeEnum 的value和含义一致
 */
public enum SignTypeEnum {
    MD5("1", "MD5"),//
    RSA("2", "RSA"),//
    RSA2("3", "RSA2");

    /** 枚举值 */
    private String value;
    /** 描述 */
    private String msg;

    private final static Map<String, String> VALUE_MAP = toValueMap();


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    private SignTypeEnum(String value, String desc) {
        this.value = value;
        this.msg = desc;
    }

    public static Map<String, String> toValueMap(){
        SignTypeEnum[] arr = SignTypeEnum.values();

        Map<String, String> map = new HashMap<>(arr.length);
        for(int i=0; i<arr.length; i++){
            map.put(arr[i].getValue(), arr[i].getMsg());
        }
        return map;
    }

    public static Map<String, String> getValueMap(){
        return VALUE_MAP;
    }

    public static int getIntValue(SignTypeEnum signType){
        return Integer.valueOf(signType.getValue());
    }
}
