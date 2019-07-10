<?php
namespace param;

/**
 * 密钥类
 * Class SecretKey
 * @package param
 */
class SecretKey {
    /**
     * 必填：发送请求时用以签名的私钥
     */
    private $reqSignPriKey;
    /**
     * 必填：对响应信息进行验签的公钥
     */
    private $respVerifyPubKey;
    /**
     * 选填：请求时，对sec_key进行加密的公钥
     */
    private $secKeyEncryptPubKey;
    /**
     * 选填：响应时，对ec_key进行解密的私钥
     */
    private $secKeyDecryptPriKey;

    /**
     * @return mixed
     */
    public function getReqSignPriKey()
    {
        return $this->reqSignPriKey;
    }

    /**
     * @param mixed $reqSignPriKey
     */
    public function setReqSignPriKey($reqSignPriKey)
    {
        $this->reqSignPriKey = $reqSignPriKey;
    }

    /**
     * @return mixed
     */
    public function getRespVerifyPubKey()
    {
        return $this->respVerifyPubKey;
    }

    /**
     * @param mixed $respVerifyPubKey
     */
    public function setRespVerifyPubKey($respVerifyPubKey)
    {
        $this->respVerifyPubKey = $respVerifyPubKey;
    }

    /**
     * @return mixed
     */
    public function getSecKeyEncryptPubKey()
    {
        return $this->secKeyEncryptPubKey;
    }

    /**
     * @param mixed $secKeyEncryptPubKey
     */
    public function setSecKeyEncryptPubKey($secKeyEncryptPubKey)
    {
        $this->secKeyEncryptPubKey = $secKeyEncryptPubKey;
    }

    /**
     * @return mixed
     */
    public function getSecKeyDecryptPriKey()
    {
        return $this->secKeyDecryptPriKey;
    }

    /**
     * @param mixed $secKeyDecryptPriKey
     */
    public function setSecKeyDecryptPriKey($secKeyDecryptPriKey)
    {
        $this->secKeyDecryptPriKey = $secKeyDecryptPriKey;
    }


}