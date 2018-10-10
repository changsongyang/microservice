package org.study.common.statics.pojos;

/**
 * @Description: REST API 操作返回的bean
 * @author: chenyf
 * @Date: 2018/1/29
 */
public class RestResult<T> extends BaseResult<T> {
    /**
     * 受理成功状态码
     */
    private static final int ACCEPT_SUCCESS_CODE = 200;
    /**
     * 受理失败状态码
     */
    private static final int ACCEPT_FAIL_CODE = 300;

    /**
     * 受理状态码
     */
    private int acceptCode;
    /**
     * 业务状态码
     */
    private int bizCode;
    /**
     * 错误码
     */
    private int errorCode;
    /**
     * 返回的提示消息
     */
    private String message;
    /**
     * 签名
     */
    private String hmac;

    public int getAcceptCode() {
        return acceptCode;
    }

    public void setAcceptCode(int acceptCode) {
        this.acceptCode = acceptCode;
    }

    public int getBizCode() {
        return bizCode;
    }

    public void setBizCode(Integer bizCode) {
        this.bizCode = bizCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public String getMessage(){
        return this.message;
    }

    public String getHmac() {
        return hmac;
    }

    public void setHmac(String hmac) {
        this.hmac = hmac;
    }

    /**
     * 受理失败
     * @return
     */
    public static <T> RestResult<T> acceptFail(String message){
        RestResult<T> resultBean = new RestResult<>();
        resultBean.setAcceptCode(ACCEPT_FAIL_CODE);
        resultBean.setMessage(message);
        return resultBean;
    }
    /**
     * 受理成功
     * @return
     */
    public static <T> RestResult<T> acceptSuccess(){
        RestResult<T> resultBean = new RestResult<>();
        resultBean.setAcceptCode(ACCEPT_SUCCESS_CODE);
        return resultBean;
    }
    /**
     * 受理成功
     * @return
     */
    public static <T> RestResult<T> acceptSuccess(T data){
        RestResult<T> resultBean = new RestResult<>();
        resultBean.setAcceptCode(ACCEPT_SUCCESS_CODE);
        resultBean.setData(data);
        return resultBean;
    }

    /**
     * 业务处理成功
     * @param data 返回的数据
     * @param <T>
     * @return
     */
    public static <T> RestResult<T> bizSuccess(Integer bizCode, T data){
        RestResult<T> resultBean = new RestResult<>();
        resultBean.setAcceptCode(ACCEPT_SUCCESS_CODE);
        resultBean.setBizCode(bizCode);
        resultBean.setData(data);
        return resultBean;
    }

    /**
     * 业务处理失败
=     * @param <T>
     * @return
     */
    public static <T> RestResult<T> bizFail(Integer bizCode, String message){
        RestResult<T> resultBean = new RestResult<>();
        resultBean.setAcceptCode(ACCEPT_SUCCESS_CODE);
        resultBean.setBizCode(bizCode);
        resultBean.setMessage(message);
        return resultBean;
    }
}
