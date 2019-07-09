package org.study.common.api.service.impl;

import com.google.common.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.study.common.api.enums.SignTypeEnum;
import org.study.common.api.params.APIParam;
import org.study.common.api.service.UserService;
import org.study.common.api.vo.MerchantInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * @description 获取商户信息的实现类，为提高性能，建议加入缓存
 * @author: chenyf
 * @Date: 2019-02-24
 */
public class UserServiceImpl implements UserService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Cache<String, MerchantInfo> cache;//使用本地缓存，避免网络传输的开销

    public UserServiceImpl(@Nullable Cache<String, MerchantInfo> cache){
        this.cache = cache;
    }

    /**
     * 根据商户编号获取商户信息
     * @param mchNo
     * @return
     */
    @Override
    public MerchantInfo getMerchantInfo(String mchNo, APIParam param){
        String key = getCacheKey(mchNo, param.getVersion());
        MerchantInfo merchantInfo = this.getFromCache(key);
        if(merchantInfo == null){
            merchantInfo = this.getFromDb(mchNo, param);
            this.storeToCache(key, merchantInfo);
        }
        return merchantInfo;
    }

    private String getCacheKey(String mchNo, String version){
        return mchNo + "_" + version;
    }

    private MerchantInfo getFromCache(String key){
        if (cache != null) {
            return (MerchantInfo) cache.getIfPresent(key);
        }
        return null;
    }

    private void storeToCache(String key, MerchantInfo merchantInfo) {
        if (cache != null && merchantInfo != null) {
            cache.put(key, merchantInfo);
        }
    }

    private MerchantInfo getFromDb(String mchNo, APIParam param){
        MerchantInfo merchantInfo = new MerchantInfo();

        String md5Key = "12345678qwertyui";
        String sysPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCCD1PquVQz6inIH66ZMndawRmihQ/4GLX/nHieaX8Htu5NZcn2hB3OZe+rk05AJgcUuUhkNqxhtkArOJJdhxxdF4BNFSQ70Zx9APuda4GgwGnpiA5yJey9awmsmUUS/k4KkQX6bLJWvbKz7TEa5Z6NDD7UBoYu6uFqZH+AL51IlQIDAQAB";
        String sysPrivateKey = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIIPU+q5VDPqKcgfrpkyd1rBGaKFD/gYtf+ceJ5pfwe27k1lyfaEHc5l76uTTkAmBxS5SGQ2rGG2QCs4kl2HHF0XgE0VJDvRnH0A+51rgaDAaemIDnIl7L1rCayZRRL+TgqRBfpssla9srPtMRrlno0MPtQGhi7q4Wpkf4AvnUiVAgMBAAECgYBHSsehHr29R1pnzJYUe8lZAghfQbkjMchxuP+VNhbfz7KI0ocGjh0Yil/6GOEH4NB416eK5z1OwmwiRPxWMD2nMFfwgSpH+tewAl6raNhTy9fumyQD6ZNs3y8swCj9e54P4Ph3B+u/OUDB1BZQu6zb2pO0FNIbFPsxPlBN5FDQcQJBAMz/RHGKG16kdTdYyHSHXLR4qtk2xik798i8i9CDJ+OnKfc8VCvGNilWoR6S4a+FcJHEhYs5QcRxNsCClmd0md8CQQCiayLa/sS2lY4dgY3n/G12cAQVhqPSyx8QGcqtLl3jTJQLUbO0fSLo542ZV4azgc/j+f0C/tML4mAY2IozktQLAkBTlQzyAi5woztLqr5ojLxmtQBr+iJHs7SuuvmCtccw0fqRXJ6xDmsM5c5hqd+s8gpY1LjicCD5mHOLgHMUkX0fAkBlR4+Vpha+kGXtalM2HUeY+mLhlXLkyHrXTG4BLg+n5KHQqSL5Yqr5NyMqQtUhbMpZLBMk4ghyubgY5jbP0DhfAkB7+lMimzHiMjLh56AZnsg3UFH+MoupkctS4oseK5vET70tSO0xiUhikf3+0BXZNhnsSfnR493ScDQbyYDKKY1d";

        String mchPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCkMtwOfeouGA5T8w8bv5xA4nV1aCTDNxU7T+kMwhkQpNT329k8S+HcIQc4t8CnivpZ5ZgquXA94MUH42S3AO3BTuCQCghhob+iWg0m9SohLh5GYloBlgI+OBFIpynib/dFfCwtLCS/afFsd4PDhrISx6M1cPv0A10QY2JOO1INpQIDAQAB";
        String mchPrivateKey = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKQy3A596i4YDlPzDxu/nEDidXVoJMM3FTtP6QzCGRCk1Pfb2TxL4dwhBzi3wKeK+lnlmCq5cD3gxQfjZLcA7cFO4JAKCGGhv6JaDSb1KiEuHkZiWgGWAj44EUinKeJv90V8LC0sJL9p8Wx3g8OGshLHozVw+/QDXRBjYk47Ug2lAgMBAAECgYA4A5WoZ/H8eX5hyxgLWklepSJ2w+lOozrd+fvBu3E7iU+RonEwLZ7GLoo9IgpZ3YJcKoPHh20v3r64Wy1fdLSmYlQ1Lk/DasEshXthwWKam+w+lBh7QS+jnChSNxlCzMebQUhKCzFV4Du28ROVVYU/UTS76+LlL5TgwOw/owSQQQJBANX1V4vw2GsS7ri7dR9gJUMl7B80/ciXEMTk1/jO6OfDfhMhWUgHPndTo+OVgyLgpagmeDFSbCCfN1Oa6kwU29UCQQDEdnn46UR1Ye0pYxu1p5YvY4wC036OX4XxLR94DShu24d104prN0ogni6pc6Jh7vtkE1LyM4sh2EiL5x/48mKRAkAUv0StAj7KKzzQ1wSldTpHx56c7BOL5vIuVY6HxvCYwMEx87LnpCQviAHFaNMdh7EonApdpgNsKmRADC6aEA+9AkB24yc+jJLD4eWttO7wx6BnvvrcPvYH3CBm6SJw+K1uIGTh1YifBw9Rm8eq/XHXh9ITJmp8bNqWOZb1KoE7mhoxAkBCxC++0ACafWKKFp8baJmILhdTu0BDKxvXflF5xWpBn2nCOY6eztJYZ9acnzI2HTL4XLe2tYFSr7V8u0e/SN0h";

        merchantInfo.setMchNo(mchNo);
        merchantInfo.setMchName("xx商户");
        merchantInfo.setMchStatus(100);
        merchantInfo.setSignType(SignTypeEnum.RSA.getValue());

        //默认使用这个共享密钥作为签名、验签、加密、解密
        if(SignTypeEnum.MD5.getValue().equals(merchantInfo.getSignType())){
            merchantInfo.setSignValidKey(md5Key);
            merchantInfo.setSignGenerateKey(md5Key);
            merchantInfo.setSecKeyDecryptKey(md5Key);
            merchantInfo.setSecKeyEncryptKey(md5Key);
        }else{
            merchantInfo.setSignValidKey(mchPublicKey);//验签用商户公钥
            merchantInfo.setSignGenerateKey(sysPrivateKey);//签名用系统密钥
            merchantInfo.setSecKeyDecryptKey(sysPrivateKey);//解密用系统密钥
            merchantInfo.setSecKeyEncryptKey(mchPublicKey);//加密用商户公钥
        }

        return merchantInfo;
    }
}
