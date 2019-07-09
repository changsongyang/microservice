package com.gw.api.base.enums;

/**
 * 响应码枚举类
 * @author: chenyf
 * @Date: 2018-12-15
 */
public enum RespCodeEnum {
    ACCEPT_SUCCESS("A1000", "受理成功"),
    ACCEPT_FAIL("A2000", "受理失败"),
    ACCEPT_UNKNOWN("A3000", "受理未知");



    /** 枚举值 */
    private String code;
    /** 描述 */
    private String msg;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    private RespCodeEnum(String code, String desc) {
        this.code = code;
        this.msg = desc;
    }
}
