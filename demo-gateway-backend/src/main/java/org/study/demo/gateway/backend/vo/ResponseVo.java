package org.study.demo.gateway.backend.vo;

import com.alibaba.fastjson.JSON;
//import com.gw.common.api.constants.CommonConst;
//import com.gw.common.api.enums.RespCodeEnum;
//import com.gw.common.api.utils.RandomUtil;

/**
 * 响应给商户的VO，主要用作Controller的出参
 * @author chenyf
 * @date 2018-12-15
 */
public class ResponseVo<T> {
    private String respCode;
    private String respMsg;
    private String mchNo;
    private T data;
    private String randStr;
    private String sign;
    private String signType;
    private String aesKey = "";
//
//    public void success(String mchNo, T data){
//        this.respCode = RespCodeEnum.ACCEPT_SUCCESS.getCode();
//        this.respMsg = RespCodeEnum.ACCEPT_SUCCESS.getMsg();
//        this.mchNo = mchNo;
//        this.randStr = RandomUtil.get32LenStr();
//        this.data = data;
//    }
//
//    public void success(String mchNo, T data, String signType) {
//        this.respCode = RespCodeEnum.ACCEPT_SUCCESS.getCode();
//        this.respMsg = RespCodeEnum.ACCEPT_SUCCESS.getMsg();
//        this.mchNo = mchNo;
//        this.randStr = RandomUtil.get32LenStr();
//        this.data = data;
//        this.signType = signType;
//    }

//    public void acceptFail(String mchNo){
//        this.respCode = RespCodeEnum.ACCEPT_FAIL.getCode();
//        this.respMsg = RespCodeEnum.ACCEPT_FAIL.getMsg();
//        this.mchNo = mchNo;
//        this.randStr = RandomUtil.get32LenStr();
//    }
//
//    public void acceptUnknown(String mchNo){
//        this.respCode = RespCodeEnum.ACCEPT_UNKNOWN.getCode();
//        this.respMsg = RespCodeEnum.ACCEPT_UNKNOWN.getMsg();
//        this.mchNo = mchNo;
//        this.randStr = RandomUtil.get32LenStr();
//    }
//
//    public void joinAesKey(String secretKey, String iv){
//        this.aesKey = secretKey + CommonConst.AES_KEY_SEPARATOR + iv;
//    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getRespMsg() {
        return respMsg;
    }

    public void setRespMsg(String respMsg) {
        this.respMsg = respMsg;
    }

    public String getMchNo() {
        return mchNo;
    }

    public void setMchNo(String mchNo) {
        this.mchNo = mchNo;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getRandStr() {
        return randStr;
    }

    public void setRandStr(String randStr) {
        this.randStr = randStr;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
