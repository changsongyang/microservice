package com.gw.api.base.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 接口版本号
 */
public enum VersionEnum {
    VERSION_1_0("1.0", "1.0"),
    VERSION_2_0("2.0", "2.0"),

    ;

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

    private VersionEnum(String value, String desc) {
        this.value = value;
        this.msg = desc;
    }

    public static Map<String, String> toValueMap(){
        VersionEnum[] arr = VersionEnum.values();

        Map<String, String> map = new HashMap<>(arr.length);
        for(int i=0; i<arr.length; i++){
            map.put(arr[i].getValue(), arr[i].getMsg());
        }
        return map;
    }

    public static Map<String, String> getValueMap(){
        return VALUE_MAP;
    }

    public static int getIntValue(VersionEnum signType){
        return Integer.valueOf(signType.getValue());
    }
}
