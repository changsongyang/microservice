package org.study.common.statics.pojos;

/**
 * @Description：基础返回信息,可用于service统一返回格式
 * @author： chenyf
 * @Version： V1.0
 * @Date： 2018/2/2 14:31
 */
public class ServiceResult<T> extends BaseResult<T> {
    /**
     * 成功码
     */
    private static final int SUCCESS_CODE = 200;
    /**
     * 失败码
     */
    private static final int FAIL_CODE = 300;

    /**
     * 状态码
     */
    private int statusCode;
    /**
     * 错误码
     */
    private int errorCode;
    /**
     * 返回的提示消息
     */
    private String message;


    public void setStatusCode(int status){
        this.statusCode = status;
    }

    public Integer getStatusCode(){
        return this.statusCode;
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

    /**
     * 成功
     * @return
     */
    public static <T> ServiceResult<T> success(){
        return success("");
    }

    /**
     * 成功
     * @return
     */
    public static <T> ServiceResult<T> success(String message){
        return success(null, message);
    }

    /**
     * 成功
     * @param data 返回的数据
     * @param <T>
     * @return
     */
    public static <T> ServiceResult<T> success(T data, String message){
        ServiceResult<T> resultBean = new ServiceResult<T>();
        resultBean.setStatusCode(SUCCESS_CODE);
        resultBean.setErrorCode(0);
        resultBean.setMessage(message);
        resultBean.setData(data);
        return resultBean;
    }

    /**
     * 失败
     * @param message 提示语
     * @param <T>
     * @return
     */
    public static <T> ServiceResult<T> fail(String message) {
        return fail(FAIL_CODE, message);
    }

    /**
     * 失败
     * @param errorCode 错误码
     * @param message 提示语
     * @param <T>
     * @return
     */
    public static <T> ServiceResult<T> fail(int errorCode, String message) {
        ServiceResult resultBean = new ServiceResult();
        resultBean.setStatusCode(FAIL_CODE);
        resultBean.setErrorCode(errorCode);
        resultBean.setMessage(message);
        return resultBean;
    }

    public Boolean isSuccess(){
        return this.statusCode == SUCCESS_CODE;
    }

    public Boolean isError(){
        return this.statusCode == FAIL_CODE;
    }
}
