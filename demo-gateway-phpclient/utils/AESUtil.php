<?php


namespace utils;


use exceptions\BizException;

class AESUtil {

    public static function encryptECB(string $data, string $secKey){
        $resultStr = openssl_encrypt($data, 'aes-128-ecb', $secKey, OPENSSL_ZERO_PADDING);
        if($resultStr === false){
            throw new BizException(BizException::$BIZ_ERROR, "aes加密失败");
        }
        return base64_encode($resultStr);
    }

    public static function decryptECB(string $data, string $secKey){
        $resultStr = openssl_decrypt(base64_decode($data), 'aes-128-ecb', $secKey, OPENSSL_ZERO_PADDING);
        if($resultStr === false){
            throw new BizException(BizException::$BIZ_ERROR, "aes解密失败");
        }
        return $resultStr;
    }
}