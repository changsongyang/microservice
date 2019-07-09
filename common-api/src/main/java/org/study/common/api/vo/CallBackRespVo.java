package com.gw.api.base.vo;

import com.gw.api.base.annonation.NotSign;
import com.gw.api.base.enums.RespCodeEnum;
import org.springframework.http.HttpStatus;

/**
 * 异步回调商户的响应VO
 * @author chenyf
 * @date 2018-12-19
 */
public class CallBackRespVo {
    /**
     * 必填：响应编码
     */
    private String resp_code;
    /**
     * 必填：响应描述
     */
    private String resp_msg;
    /**
     * 选填：响应数据
     */
    private String data;
    /**
     * 选填：签名随机串
     */
    private String rand_str;
    /**
     * 选填：签名类型
     */
    private String sign_type;
    /**
     * 选填：商户编号
     */
    private String mch_no;
    /**
     * 选填：签名串
     */
    @NotSign
    private String sign;
    /**
     * 选填：对敏感数据加解密的sec_key
     */
    @NotSign
    private String sec_key;
    @NotSign
    private int httpStatus;

    public static CallBackRespVo defaultResp(){
        CallBackRespVo respVo = new CallBackRespVo();
        respVo.setResp_code(RespCodeEnum.ACCEPT_SUCCESS.getCode());
        respVo.setResp_msg("默认值");
        respVo.setHttpStatus(HttpStatus.OK.value());
        return respVo;
    }

    public boolean isSuccess(){
        return RespCodeEnum.ACCEPT_SUCCESS.getCode().equals(this.resp_code);
    }

    public String getResp_code() {
        return resp_code;
    }

    public void setResp_code(String resp_code) {
        this.resp_code = resp_code;
    }

    public String getResp_msg() {
        return resp_msg;
    }

    public void setResp_msg(String resp_msg) {
        this.resp_msg = resp_msg;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getRand_str() {
        return rand_str;
    }

    public void setRand_str(String rand_str) {
        this.rand_str = rand_str;
    }

    public String getSign_type() {
        return sign_type;
    }

    public void setSign_type(String sign_type) {
        this.sign_type = sign_type;
    }

    public String getMch_no() {
        return mch_no;
    }

    public void setMch_no(String mch_no) {
        this.mch_no = mch_no;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getSec_key() {
        return sec_key;
    }

    public void setSec_key(String sec_key) {
        this.sec_key = sec_key;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }
}
