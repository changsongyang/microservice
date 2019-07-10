<?php


namespace utils;


use exceptions\BizException;

/**
 * AES加解密工具类
 * Class AESUtil
 * @package utils
 */
class AESUtil {
    const EBC_MODE = "AES-128-ECB";
//    const CBC_MODE = "AES-128-CBC";

    /**
     * AES加密，模式为：AES/ECB/PKCK7Padding
     * @param string $data
     * @param string $secKey
     * @return string
     * @throws BizException
     */
    public static function encryptECB(string $data, string $secKey){
        $encrypted = openssl_encrypt($data, self::EBC_MODE, $secKey, OPENSSL_RAW_DATA);
        if($encrypted === false){
            throw new BizException(BizException::BIZ_ERROR, "aes加密失败");
        }
        return base64_encode($encrypted);
    }

    /**
     * AES解密，模式为：AES/ECB/PKCK7Padding
     * @param string $data
     * @param string $secKey
     * @return string
     * @throws BizException
     */
    public static function decryptECB(string $data, string $secKey){
        $decrypted = openssl_decrypt(base64_decode($data), self::EBC_MODE, $secKey, OPENSSL_RAW_DATA);
        if($decrypted === false){
            throw new BizException(BizException::BIZ_ERROR, "aes解密失败");
        }
        return $decrypted;
    }

//    public static function encryptCBC(string $data, string $secKey, string $iv){
//        $encrypted = openssl_encrypt($data, self::CBC_MODE, $secKey, OPENSSL_RAW_DATA, $iv);
//        if($encrypted === false){
//            throw new BizException(BizException::BIZ_ERROR, "aes加密失败");
//        }
//        return base64_encode($encrypted);
//    }

//    public static function decryptCBC(string $data, string $secKey, string $iv){
//        $decrypted = openssl_decrypt(base64_decode($data), self::CBC_MODE, $secKey, OPENSSL_RAW_DATA, $iv);
//        if($decrypted === false){
//            throw new BizException(BizException::BIZ_ERROR, "aes解密失败");
//        }
//        return self::pkcs5_unpad($decrypted);
//    }
}