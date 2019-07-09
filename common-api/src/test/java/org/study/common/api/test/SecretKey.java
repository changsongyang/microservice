package org.study.common.api.test;

public class SecretKey {
    /**
     * 必填：发送请求时用以签名的密钥
     */
    private String reqSignPriKey;
    /**
     * 必填：对响应信息进行验签的公钥
     */
    private String respVerifyPubKey;
    /**
     * 选填：请求时，对sec_key进行加密的公钥
     */
    private String secKeyEncryptPubKey;
    /**
     * 选填：响应时，对ec_key进行解密密的密钥
     */
    private String secKeyDecryptPriKey;

    public String getReqSignPriKey() {
        return reqSignPriKey;
    }

    public void setReqSignPriKey(String reqSignPriKey) {
        this.reqSignPriKey = reqSignPriKey;
    }

    public String getRespVerifyPubKey() {
        return respVerifyPubKey;
    }

    public void setRespVerifyPubKey(String respVerifyPubKey) {
        this.respVerifyPubKey = respVerifyPubKey;
    }

    public String getSecKeyEncryptPubKey() {
        return secKeyEncryptPubKey;
    }

    public void setSecKeyEncryptPubKey(String secKeyEncryptPubKey) {
        this.secKeyEncryptPubKey = secKeyEncryptPubKey;
    }

    public String getSecKeyDecryptPriKey() {
        return secKeyDecryptPriKey;
    }

    public void setSecKeyDecryptPriKey(String secKeyDecryptPriKey) {
        this.secKeyDecryptPriKey = secKeyDecryptPriKey;
    }
}
