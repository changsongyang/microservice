package org.study.demo.gateway.backend.vo;

import com.alibaba.fastjson.JSON;

/**
 * 商户请求参数的VO，主要用作Controller的入参
 * @author chenyf
 * @date 2018-12-15
 */
public class RequestVo<T> {
    private String method;
    private String version;
    private T data;
    private String randStr;
    private String signType;
    private String mchNo;
    private String sign;
    private String aesKey;

    public String[] splitAesKey(){
        return this.aesKey.split(":");
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getMchNo() {
        return mchNo;
    }

    public void setMchNo(String mchNo) {
        this.mchNo = mchNo;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
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
