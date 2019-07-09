package org.study.common.api.exceptions;


import org.study.common.api.enums.BizCodeEnum;
import org.study.common.api.enums.RespCodeEnum;

/**
 * @Description: api接口层专用异常类
 * @author: chenyf
 * @Date: 2018/1/29
 */
public class ApiException extends RuntimeException {
    /**
     * 响应码
     */
    protected String respCode;

    /**
     * 业务码
     */
    protected String bizCode;

    /**
     * 业务码描述
     */
    protected String bizMsg;

    /**
     * 内部码，可供内部处理时使用
     */
    protected int innerCode;

    private ApiException() {
        super();
    }

    public ApiException(String msg) {
        super(msg);
    }

    public ApiException(String msg, Throwable t) {
        super(msg, t);
    }

    public ApiException innerCode(int innerCode){
        this.innerCode = innerCode;
        return this;
    }

    /**
     * 抛出受理失败的异常
     * @param bizCode
     * @param bizMsg
     * @return
     */
    public static ApiException acceptFail(String bizCode, String bizMsg){
        ApiException exception = new ApiException(bizCode + ", " + bizMsg);
        exception.respCode = RespCodeEnum.ACCEPT_FAIL.getCode();
        exception.bizCode = bizCode;
        exception.bizMsg = bizMsg;
        return exception;
    }

    /**
     * 抛出受理结果未知的异常
     * @return
     */
    public static ApiException acceptUnknown(){
        ApiException exception = new ApiException(BizCodeEnum.ACCEPT_UNKNOWN.getMsg());
        exception.respCode = RespCodeEnum.ACCEPT_UNKNOWN.getCode();
        exception.bizCode = BizCodeEnum.ACCEPT_UNKNOWN.getCode();
        exception.bizMsg = BizCodeEnum.ACCEPT_UNKNOWN.getMsg();
        return exception;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getBizCode() {
        return bizCode;
    }

    public void setBizCode(String bizCode) {
        this.bizCode = bizCode;
    }

    public String getBizMsg() {
        return bizMsg;
    }

    public void setBizMsg(String bizMsg) {
        this.bizMsg = bizMsg;
    }

    public int getInnerCode() {
        return innerCode;
    }

    public void setInnerCode(int innerCode) {
        this.innerCode = innerCode;
    }
}
