package org.study.common.util.utils;

import org.study.common.statics.exceptions.BizException;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * RSA加解密的工具类
 * @author chenyf
 * @date 2018-12-15
 */
public class RSAUtil {
    public final static String ALG_RSA = "RSA";
    public static final String ENCODING_UTF_8 = "UTF-8";

    public static final String SIGNATURE_ALGORITHM_MD5 = "MD5withRSA";
    public static final String SIGNATURE_ALGORITHM_SHA1 = "SHA1withRSA";

    public static final String ANDROID_ENCRYPT_ALGORITHM = "RSA/ECB/NoPadding";
    public static final String DEFAULT_ENCRYPT_ALGORITHM = "RSA/ECB/PKCS1Padding";

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 116;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;


    public static final String PUBLIC_KEY = "publicKey";
    public static final String PRIVATE_KEY = "privateKey";


    /**
     * 生成公私密钥对
     * @return
     * @throws org.study.common.statics.exceptions.BizException
     */
    public static Map<String, String> genKeyPair() throws BizException {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(ALG_RSA);
            keyPairGen.initialize(1024);
            KeyPair keyPair = keyPairGen.generateKeyPair();

            Map<String, String> keyMap = new HashMap<>(2);
            keyMap.put(PUBLIC_KEY, CodeUtil.base64Encode(keyPair.getPublic().getEncoded()));
            keyMap.put(PRIVATE_KEY, CodeUtil.base64Encode(keyPair.getPrivate().getEncoded()));
            return keyMap;
        }catch (Throwable e){
            throw new BizException("生成RSA密钥对出现异常", e);
        }
    }

    /**
     * 生成RSA签名串
     * @param data          需要生成签名串的数据
     * @param privateKey    私钥
     * @return
     * @throws BizException
     */
    public static String sign(String data, String privateKey) throws BizException {
        try {
            byte[] dataBytes = data.getBytes(ENCODING_UTF_8);
            byte[] keyBytes = CodeUtil.base64Decode(privateKey);
            String algorithm = SIGNATURE_ALGORITHM_MD5;

            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
            PrivateKey priKey = KeyFactory.getInstance(ALG_RSA).generatePrivate(pkcs8KeySpec);
            Signature signature = Signature.getInstance(algorithm);
            signature.initSign(priKey);
            signature.update(dataBytes);
            return CodeUtil.base64Encode(signature.sign());
        }catch (Throwable e){
            throw new BizException("生成RSA签名失败", e);
        }
    }

    /**
     * 验证RSA签名串
     * @param data      需要验签的数据
     * @param publicKey 公钥
     * @param sign      用户传过来的签名串
     * @return
     * @throws BizException
     */
    public static boolean verify(String data, String publicKey, String sign) throws BizException {
        try {
            byte[] dataBytes = data.getBytes(ENCODING_UTF_8);
            byte[] signBytes = CodeUtil.base64Decode(sign);
            String algorithm = SIGNATURE_ALGORITHM_MD5;
            PublicKey publicK = getPublicKey(publicKey);

            Signature signature = Signature.getInstance(algorithm);
            signature.initVerify(publicK);
            signature.update(dataBytes);
            return signature.verify(signBytes);
        }catch (Throwable e){
            throw new BizException("RSA验签失败", e);
        }
    }

    /**
     * 使用RSA进行加密
     * @param data      需要加密的数据
     * @param publicKey 公钥
     * @param isAndroid 是否属于安卓设备
     * @return
     */
    public static String encrypt(String data, String publicKey, boolean isAndroid) throws BizException {
        try{
            byte[] dataBytes = data.getBytes(ENCODING_UTF_8);
            dataBytes = doCipher(dataBytes, publicKey, true, isAndroid);
            return CodeUtil.base64Encode(dataBytes);
        }catch(Throwable e){
            throw new BizException("RSA加密失败", e);
        }
    }

    /**
     * 使用RSA进行解密
     * @param data       需要解密的数据
     * @param privateKey 私钥
     * @return
     */
    public static String decrypt(String data, String privateKey) throws BizException {
        try{
            byte[] dataBytes = CodeUtil.base64Decode(data);
            dataBytes = doCipher(dataBytes, privateKey, false, false);
            return new String(dataBytes, ENCODING_UTF_8);
        }catch(Throwable e){
            throw new BizException("RSA解密失败", e);
        }
    }

    private static byte[] doCipher(byte[] dataBytes, String keyStr, boolean isEncrypt, boolean isAndroid) throws Exception{
        Key key;
        Cipher cipher;
        int maxBlock;

        if(isEncrypt){
            maxBlock = MAX_ENCRYPT_BLOCK;
            key = getPublicKey(keyStr);
            if(isAndroid){
                cipher = Cipher.getInstance(ANDROID_ENCRYPT_ALGORITHM);// 如果是安卓机
            }else{
                cipher = Cipher.getInstance(DEFAULT_ENCRYPT_ALGORITHM);
            }
            cipher.init(Cipher.ENCRYPT_MODE, key);
        }else{
            maxBlock = MAX_DECRYPT_BLOCK;
            byte[] keyBytes = CodeUtil.base64Decode(keyStr);
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALG_RSA);
            Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
            cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, privateK);
        }

        int offSet = 0, i = 0, inputLen = dataBytes.length;
        byte[] cache;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try{
            // 对数据分段加密/解密
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > maxBlock) {
                    cache = cipher.doFinal(dataBytes, offSet, maxBlock);
                } else {
                    cache = cipher.doFinal(dataBytes, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * maxBlock;
            }
            return out.toByteArray();
        } finally {
            out.close();
        }
    }

    private static PublicKey getPublicKey(String publicKey) throws Exception{
        byte[] keyBytes = CodeUtil.base64Decode(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALG_RSA);
        return keyFactory.generatePublic(x509KeySpec);
    }


    public static void main(String[] args){
        Map<String, String> keyMap = genKeyPair();
        Map<String, String> keyMap2 = genKeyPair();

        String data = "都是交流交流发就发给对方感到我认为日u我认465dff34DWS34PO发的发生的34343，。？@！#%￥%~,;'=》》‘；【】@发生的开发商的方式飞机克里斯多夫快回家的思考方式对方老师的讲课费";

        String sign = sign(data, keyMap.get(PRIVATE_KEY));//使用私钥签名

        boolean isOk_1 = verify(data, keyMap.get(PUBLIC_KEY), sign);//使用公钥验签
        boolean isOk_2 = verify(data, keyMap2.get(PUBLIC_KEY), sign);

        System.out.println("isOk_1="+isOk_1);
        System.out.println("isOk_2="+isOk_2);

        String encrypted = encrypt(data, keyMap.get(PUBLIC_KEY), false);
        System.out.println("encrypted = " + encrypted);
        System.out.println("decrypted = " + decrypt(encrypted, keyMap.get(PRIVATE_KEY)));

//        String dataEncrypt = encrypt(data, keyMap.get(PUBLIC_KEY), false);
//        String dataDecrypt = decrypt(dataEncrypt, keyMap.get(PRIVATE_KEY));//使用正确的私钥解密
//        System.out.println("dataEncrypt="+dataEncrypt);
//        System.out.println("dataDecrypt="+dataDecrypt);
//        System.out.println("data.equals(dataDecrypt) ? " + (data).equals(dataDecrypt));
//
//        String dataDecrypt2 = decrypt(dataEncrypt, keyMap2.get(PRIVATE_KEY));//使用错误的私钥解密
//        System.out.println("dataDecrypt2="+dataDecrypt2);

        data = "都是交流交流发就发给对方感到我认为日u我认465dff34DWS34PO发的发生的34343，。？@！#%￥%~,;'=》》‘；【】@";
        String mchPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCkMtwOfeouGA5T8w8bv5xA4nV1aCTDNxU7T+kMwhkQpNT329k8S+HcIQc4t8CnivpZ5ZgquXA94MUH42S3AO3BTuCQCghhob+iWg0m9SohLh5GYloBlgI+OBFIpynib/dFfCwtLCS/afFsd4PDhrISx6M1cPv0A10QY2JOO1INpQIDAQAB";
        String mchPrivateKey = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKQy3A596i4YDlPzDxu/nEDidXVoJMM3FTtP6QzCGRCk1Pfb2TxL4dwhBzi3wKeK+lnlmCq5cD3gxQfjZLcA7cFO4JAKCGGhv6JaDSb1KiEuHkZiWgGWAj44EUinKeJv90V8LC0sJL9p8Wx3g8OGshLHozVw+/QDXRBjYk47Ug2lAgMBAAECgYA4A5WoZ/H8eX5hyxgLWklepSJ2w+lOozrd+fvBu3E7iU+RonEwLZ7GLoo9IgpZ3YJcKoPHh20v3r64Wy1fdLSmYlQ1Lk/DasEshXthwWKam+w+lBh7QS+jnChSNxlCzMebQUhKCzFV4Du28ROVVYU/UTS76+LlL5TgwOw/owSQQQJBANX1V4vw2GsS7ri7dR9gJUMl7B80/ciXEMTk1/jO6OfDfhMhWUgHPndTo+OVgyLgpagmeDFSbCCfN1Oa6kwU29UCQQDEdnn46UR1Ye0pYxu1p5YvY4wC036OX4XxLR94DShu24d104prN0ogni6pc6Jh7vtkE1LyM4sh2EiL5x/48mKRAkAUv0StAj7KKzzQ1wSldTpHx56c7BOL5vIuVY6HxvCYwMEx87LnpCQviAHFaNMdh7EonApdpgNsKmRADC6aEA+9AkB24yc+jJLD4eWttO7wx6BnvvrcPvYH3CBm6SJw+K1uIGTh1YifBw9Rm8eq/XHXh9ITJmp8bNqWOZb1KoE7mhoxAkBCxC++0ACafWKKFp8baJmILhdTu0BDKxvXflF5xWpBn2nCOY6eztJYZ9acnzI2HTL4XLe2tYFSr7V8u0e/SN0h";
        sign = sign(data, mchPrivateKey);//使用私钥签名
        boolean isOk_3 = verify(data, mchPublicKey, sign);//使用公钥验签
        System.out.println("isOk_3="+isOk_3);
    }
}
