<?php

namespace utils;


use exceptions\BizException;

class RSAUtil
{

    /**
     * 使用公钥加密
     * @param string $data
     * @param string $pubKey
     * @return string
     */
    public static function encrypt(string $data, string $pubKey){
        $pubKey = openssl_get_publickey($pubKey);
        if($pubKey === false){
            throw new BizException(BizException::$BIZ_ERROR, "rsa解密公钥无效");
        }

        $crypted = '';
        $isSuccess = openssl_public_encrypt($data, $crypted, $pubKey);
        openssl_free_key($pubKey);
        if(! $isSuccess){
            throw new BizException(BizException::$BIZ_ERROR, "rsa加密失败");
        }
        return base64_encode($crypted);
    }

    /**
     * 使用私钥解密
     * @param string $data
     * @param string $priKey
     * @return string
     */
    public static function decrypt(string $data, string $priKey){
        $priKey = openssl_get_privatekey($priKey);
        if($priKey === false){
            throw new BizException(BizException::$BIZ_ERROR, "rsa解密私钥无效");
        }

        $decrypted = ’‘;
        $isSuccess = openssl_private_decrypt(base64_decode($data), $decrypted, $priKey);
        openssl_free_key($priKey);
        if(! $isSuccess){
            throw new BizException(BizException::$BIZ_ERROR, "rsa解密失败");
        }
        return $decrypted;
    }

    public static function sign(string $data, string $priKey){
        $priKey = openssl_get_privatekey($priKey);
        if($priKey === false){
            throw new BizException(BizException::$BIZ_ERROR, "rsa签名私钥无效");
        }

        $binary_signature = "";
        $isSuccess = openssl_sign($data, $binary_signature, $priKey);
        openssl_free_key($priKey);
        if(! $isSuccess){
            throw new BizException(BizException::$BIZ_ERROR, "rsa签名失败");
        }
        return base64_encode($binary_signature);
    }

    public static function verify(string $signData, string $signParam, string $pubKey){
        $pubKey = openssl_get_publickey($pubKey);
        if($pubKey === false){
            throw new BizException(BizException::$BIZ_ERROR, "rsa验签公钥无效");
        }

        $isMatch = openssl_verify($signData, base64_decode($signParam), $pubKey) === 1;
        openssl_free_key($pubKey);
        return $isMatch;
    }
}