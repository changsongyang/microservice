package com.gw.api.base.params;

/**
 * API处理过程中的额外参数，主要用作预留，方便日后做一些拓展变动
 */
public class APIParam {
    /**
     * 是否填充签名类型(加签名时用)
     */
    private boolean fillSignType;
    /**
     * 是否对响应信息进行验签
     */
    private boolean respSignVerify;
    /**
     * 接口版本号
     */
    private String version;

    public APIParam(){}

    public APIParam(String version){
        this.version = version;
    }

    public boolean getFillSignType() {
        return fillSignType;
    }

    public void setFillSignType(boolean fillSignType) {
        this.fillSignType = fillSignType;
    }

    public boolean getRespSignVerify() {
        return respSignVerify;
    }

    public void setRespSignVerify(boolean respSignVerify) {
        this.respSignVerify = respSignVerify;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
