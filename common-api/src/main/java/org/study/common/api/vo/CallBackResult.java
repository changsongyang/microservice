package com.gw.api.base.vo;

import com.gw.api.base.enums.RespCodeEnum;
import com.gw.api.base.utils.StringUtil;
import org.springframework.http.HttpStatus;

public class CallBackResult<T> {
    public final static int RESULT_TYPE_SUCCESS = 200;//回调成功
    public final static int RESULT_TYPE_NETWORK_ERROR = 500;//网络原因失败：主要包括408、504
    public final static int RESULT_TYPE_CONTENT_WRONG = 501;//响应内容格式不符
    public final static int RESULT_TYPE_MCH_SERVER_FAIL = 502;//商户侧异常，包括：给了错误的回调地址、商户服务器异常、mediaType错误等等
    public final static int RESULT_TYPE_MCH_SET_FAIL = 503;//商户主动置为失败

    private int resultType;
    private String msg;
    private boolean isSignPass = false;//签名校验是否通过
    private boolean signVerify;//是否需要验签，一般是需要根据商户的响应情况做不同的业务处理的情况才需要对响应信息进行验签
    private CallBackRespVo respVo;//预留字段

    private CallBackResult(){}

    public static CallBackResult from(CallBackRespVo respVo){
        int resultType = 0; String msg = "";
        HttpStatus status = HttpStatus.resolve(respVo.getHttpStatus());
        if(status == null){
            resultType = RESULT_TYPE_MCH_SERVER_FAIL;
            msg = "错误的网络响应码:"+respVo.getHttpStatus();
        }else{
            if(status.isError()){
                if(status.equals(HttpStatus.REQUEST_TIMEOUT) || status.equals(HttpStatus.GATEWAY_TIMEOUT)){
                    resultType = RESULT_TYPE_NETWORK_ERROR;
                    msg = "网络超时";
                }else{
                    resultType = RESULT_TYPE_MCH_SERVER_FAIL;
                    msg = status.getReasonPhrase();
                }
            }else if(StringUtil.isEmpty(respVo.getResp_code())){
                resultType = RESULT_TYPE_CONTENT_WRONG;
                msg = "响应格式错误，resp_code为空";
            }else if(RespCodeEnum.ACCEPT_SUCCESS.getCode().equals(respVo.getResp_code())){
                resultType = RESULT_TYPE_SUCCESS;
                msg = "SUCCESS";
            }else if(RespCodeEnum.ACCEPT_FAIL.getCode().equals(respVo.getResp_code()) ||
                    RespCodeEnum.ACCEPT_UNKNOWN.getCode().equals(respVo.getResp_code())){
                resultType = RESULT_TYPE_MCH_SET_FAIL;
                msg = "商户要求重试";
            }else{
                resultType = RESULT_TYPE_SUCCESS;
                msg = "未预期结果 httpStatus="+respVo.getHttpStatus()+",resp_code="+respVo.getResp_code();
            }
        }

        CallBackResult result = new CallBackResult();
        result.setResultType(resultType);
        result.setMsg(msg);
        result.setRespVo(respVo);
        return result;
    }

    /**
     * 是否适合验签
     * @return
     */
    public boolean isCanVerifySign(){
        return resultType == RESULT_TYPE_SUCCESS || resultType == RESULT_TYPE_MCH_SET_FAIL;
    }
    public boolean isMchRetry(){
        return resultType == RESULT_TYPE_MCH_SET_FAIL;
    }
    public boolean isSuccess(){
        return resultType == RESULT_TYPE_SUCCESS;
    }
    public boolean isNetworkError(){
        return resultType == RESULT_TYPE_NETWORK_ERROR;
    }
    public boolean isNoNeedRetry(){
        return ! isNeedRetry();
    }
    public boolean isNeedRetry(){
        return resultType == RESULT_TYPE_NETWORK_ERROR || resultType == RESULT_TYPE_MCH_SET_FAIL;
    }
    public int getResultType() {
        return resultType;
    }

    public void setResultType(int resultType) {
        this.resultType = resultType;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public CallBackRespVo getRespVo() {
        return respVo;
    }

    public void setRespVo(CallBackRespVo respVo) {
        this.respVo = respVo;
    }

    public boolean isSignPass() {
        return isSignPass;
    }

    public void setSignPass(boolean signPass) {
        isSignPass = signPass;
    }

    public boolean isSignVerify() {
        return signVerify;
    }

    public void setSignVerify(boolean signVerify) {
        this.signVerify = signVerify;
    }
}
