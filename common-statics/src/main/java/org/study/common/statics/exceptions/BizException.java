package org.study.common.statics.exceptions;

import java.io.Serializable;

/**
 * 业务异常类
 * Created by jo on 2018/9/3.
 */
public class BizException extends RuntimeException implements Serializable{
    private static final long serialVersionUID = -345568986985960990L;
    //参数校验异常
    public final static int PARAM_VALIDATE_ERROR = 100001001;
    //业务校验异常
    public final static int BIZ_VALIDATE_ERROR = 100001002;
    //数据库记录数不匹配
    public final static int DB_UPDATE_RESULT_NOT_MATCH = 100001003;


    /**
     * 异常信息
     */
    protected String msg;
    /**
     * 具体异常码
     */
    protected int code;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public BizException() {
        super();
    }

    public BizException(int errorCode, String message) {
        super(message);
        this.code = errorCode;
        this.msg = message;
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }

    public BizException(String message) {
        super(message);
    }

    public BizException(Throwable cause) {
        super(cause);
    }
}
