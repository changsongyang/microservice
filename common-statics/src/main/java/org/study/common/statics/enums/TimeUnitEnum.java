package org.study.common.statics.enums;

/**
 * Created by jo on 2017/9/1.
 */
public enum TimeUnitEnum {
    SECONDS(1, "秒"),
    MINUTES(2, "分"),
    HOURS(3, "小时");

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
