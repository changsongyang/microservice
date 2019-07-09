package org.study.common.api.utils;

import org.study.common.api.exceptions.ApiException;

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
    public static final String PUBLIC_KEY = "publicKey";
    public static final String PRIVATE_KEY = "privateKey";
    public static final String SIGNATURE_ALGORITHM_SHA1 = "SHA1withRSA";
    public static final String SIGNATURE_ALGORITHM_MD5 = "MD5withRSA";

    public static final int KEY_SIZE = 1024;

    public static final String ANDROID_ENCRYPT_ALGORITHM = "RSA/ECB/NoPadding";
    public static final String DEFAULT_ENCRYPT_ALGORITHM = "RSA/ECB/PKCS1Padding";

    /** */
    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 116;

    /** */
    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;


    /**
     * 生成公私密钥对
     * @return
     * @throws ApiException
     */
    public static Map<String, String> genKeyPair() throws ApiException {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(Algorithm.RSA);
            keyPairGen.initialize(KEY_SIZE);
            KeyPair keyPair = keyPairGen.generateKeyPair();

            Map<String, String> keyMap = new HashMap<>(2);
            keyMap.put(PUBLIC_KEY, CodeUtil.base64Encode(keyPair.getPublic().getEncoded()));
            keyMap.put(PRIVATE_KEY, CodeUtil.base64Encode(keyPair.getPrivate().getEncoded()));
            return keyMap;
        }catch (Throwable e){
            throw new ApiException("生成RSA密钥对出现异常", e);
        }
    }

    /**
     * 生成RSA签名串
     * @param data          需要生成签名串的数据
     * @param privateKey    私钥
     * @return
     * @throws ApiException
     */
    public static String sign(String data, String privateKey, boolean isSha) throws ApiException{
        try {
            byte[] dataBytes = data.getBytes(CommonConst.ENCODING_UTF_8);
            byte[] keyBytes = CodeUtil.base64Decode(privateKey);
            String algorithm = isSha ? SIGNATURE_ALGORITHM_SHA1 : SIGNATURE_ALGORITHM_MD5;

            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
            PrivateKey priKey = KeyFactory.getInstance(Algorithm.RSA).generatePrivate(pkcs8KeySpec);
            Signature signature = Signature.getInstance(algorithm);
            signature.initSign(priKey);
            signature.update(dataBytes);
            return HEXUtil.encode(CodeUtil.base64Encode(signature.sign()));
        }catch (Throwable e){
            throw new ApiException("生成RSA签名失败", e);
        }
    }

    /**
     * 验证RSA签名串
     * @param data      需要验签的数据
     * @param publicKey 公钥
     * @param sign      用户传过来的签名串
     * @return
     * @throws ApiException
     */
    public static boolean verify(String data, String publicKey, String sign, boolean isSha) throws ApiException {
        try {
            byte[] dataBytes = data.getBytes(CommonConst.ENCODING_UTF_8);
            byte[] signBytes = CodeUtil.base64Decode(HEXUtil.decode(sign));
            String algorithm = isSha ? SIGNATURE_ALGORITHM_SHA1 : SIGNATURE_ALGORITHM_MD5;
            PublicKey publicK = getPublicKey(publicKey);

            Signature signature = Signature.getInstance(algorithm);
            signature.initVerify(publicK);
            signature.update(dataBytes);
            return signature.verify(signBytes);
        }catch (Throwable e){
            throw new ApiException("RSA验签失败", e);
        }
    }

    /**
     * 使用RSA进行加密
     * @param data      需要加密的数据
     * @param publicKey 公钥
     * @param isAndroid 是否属于安卓设备
     * @return
     */
    public static String encrypt(String data, String publicKey, boolean isAndroid) throws ApiException {
        try{
            byte[] dataBytes = data.getBytes(CommonConst.ENCODING_UTF_8);
            dataBytes = doCipher(dataBytes, publicKey, true, isAndroid);
            return HEXUtil.encode(CodeUtil.base64Encode(dataBytes));
        }catch(Throwable e){
            throw new ApiException("RSA加密失败", e);
        }
    }

    /**
     * 使用RSA进行解密
     * @param data       需要解密的数据
     * @param privateKey 私钥
     * @return
     */
    public static String decrypt(String data, String privateKey) throws ApiException {
        try{
            byte[] dataBytes = CodeUtil.base64Decode(HEXUtil.decode(data));
            dataBytes = doCipher(dataBytes, privateKey, false, false);
            return new String(dataBytes, CommonConst.ENCODING_UTF_8);
        }catch(Throwable e){
            throw new ApiException("RSA解密失败", e);
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
            KeyFactory keyFactory = KeyFactory.getInstance(Algorithm.RSA);
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
        KeyFactory keyFactory = KeyFactory.getInstance(Algorithm.RSA);
        return keyFactory.generatePublic(x509KeySpec);
    }


    public static void main(String[] args){
        Map<String, String> keyMap = genKeyPair();
        Map<String, String> keyMap2 = genKeyPair();

        String data = "都是交流交流发就发给对方感到我认为日u我认465dff34DWS34PO发的发生的34343，。？@！#%￥%~,;'=》》‘；【】@";

        String sign = sign(data, keyMap.get(PRIVATE_KEY), true);//使用私钥签名

        boolean isOk_1 = verify(data, keyMap.get(PUBLIC_KEY), sign, true);//使用公钥验签
        boolean isOk_2 = verify(data, keyMap2.get(PUBLIC_KEY), sign, true);

        System.out.println("isOk_1="+isOk_1);
        System.out.println("isOk_2="+isOk_2);

        String dataEncrypt = encrypt(data, keyMap.get(PUBLIC_KEY), false);
        String dataDecrypt = decrypt(dataEncrypt, keyMap.get(PRIVATE_KEY));//使用正确的私钥解密
        System.out.println("dataEncrypt="+dataEncrypt);
        System.out.println("dataDecrypt="+dataDecrypt);
        System.out.println("data.equals(dataDecrypt) ? " + (data).equals(dataDecrypt));

        String dataDecrypt2 = decrypt(dataEncrypt, keyMap2.get(PRIVATE_KEY));//使用错误的私钥解密
        System.out.println("dataDecrypt2="+dataDecrypt2);
    }
}
