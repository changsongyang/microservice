package com.joinpay.sdk.entity;

public class SecretKey {
    /**
     * 必填：发送请求时用以签名的商户密钥
     */
    private String mchSignPrivateKey;
    /**
     * 必填：汇聚响应时，用以验证签名的汇聚公钥
     */
    private String jpVerifyPublicKey;

    /**
     * 选填：发送请求时，用以对aes_key进行加密的汇聚公钥
     */
    private String jpEncryptPublicKey;
    /**
     * 选填：汇聚响应时，用以对aes_key进行解密密的商户密钥
     */
    private String mchDecryptPrivateKey;

    public String getMchSignPrivateKey() {
        return mchSignPrivateKey;
    }

    public void setMchSignPrivateKey(String mchSignPrivateKey) {
        this.mchSignPrivateKey = mchSignPrivateKey;
    }

    public String getJpVerifyPublicKey() {
        return jpVerifyPublicKey;
    }

    public void setJpVerifyPublicKey(String jpVerifyPublicKey) {
        this.jpVerifyPublicKey = jpVerifyPublicKey;
    }

    public String getJpEncryptPublicKey() {
        return jpEncryptPublicKey;
    }

    public void setJpEncryptPublicKey(String jpEncryptPublicKey) {
        this.jpEncryptPublicKey = jpEncryptPublicKey;
    }

    public String getMchDecryptPrivateKey() {
        return mchDecryptPrivateKey;
    }

    public void setMchDecryptPrivateKey(String mchDecryptPrivateKey) {
        this.mchDecryptPrivateKey = mchDecryptPrivateKey;
    }
}
