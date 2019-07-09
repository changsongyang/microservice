package org.study.common.api.enums;

/**
 * 订单状态枚举类
 * @author: chenyf
 * @Date: 2018-12-15
 */
public enum OrderStatusEnum {
    PENDING("P0001", "待处理中"),
    SUCCESS("P1000", "交易成功"),
    FAIL("P2000", "交易失败"),
    UNKNOWN("P3000", "交易未知"),
    PROCESSING("P3001", "交易处理中"),
    ACTIVE("P3002", "激活"),
    FROZEN("P3003", "冻结"),


    ;


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

    private OrderStatusEnum(String code, String desc) {
        this.code = code;
        this.msg = desc;
    }
}
