package com.xpay.facade.timer.enums;

public enum TimerStatus {
    RUNNING(1, "运行中"),
    STAND_BY(2, "挂起中")
    ;


    private int value;
    private String desc;

    private TimerStatus(int value, String desc){
        this.value = value;
        this.desc = desc;
    }

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
