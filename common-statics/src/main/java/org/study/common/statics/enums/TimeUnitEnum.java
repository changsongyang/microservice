package org.study.common.statics.enums;

/**
 * Created by jo on 2017/9/1.
 */
public enum TimeUnitEnum {
    MILL_SECOND(1, "毫秒"),
    SECOND(2, "秒"),
    MINUTE(3, "分"),
    HOUR(4, "小时");

    private TimeUnitEnum(int value, String desc){
        this.value = value;
        this.desc = desc;
    }

    private int value;
    private String desc;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


}
