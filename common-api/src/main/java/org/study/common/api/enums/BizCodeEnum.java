package org.study.common.api.enums;

/**
 * 业务码枚举类
 * @author: chenyf
 * @Date: 2018-12-15
 */
public enum BizCodeEnum {
    ACCEPT_SUCCESS("B100000", "受理成功"),
    SIGN_VALID_FAIL("B100001", "验签失败"),
    PARAM_VALID_FAIL("B100002", "验参失败"),
    ACCEPT_UNKNOWN("B100003", "结果未知");


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

    private BizCodeEnum(String code, String desc) {
        this.code = code;
        this.msg = desc;
    }
}
