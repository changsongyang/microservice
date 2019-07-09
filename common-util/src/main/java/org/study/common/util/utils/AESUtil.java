package org.study.common.api.utils;

import com.gw.api.base.constants.Algorithm;
import com.gw.api.base.constants.CommonConst;
import com.gw.api.base.exceptions.ApiException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * AES加解密工具类
 * @author chenyf
 * @date 2018-12-15
 */
public class AESUtil {
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";//算法/模式/补码方式

    public static String encrypt(String content, String password, String ivParam){
        try {
            SecretKeySpec secSpec = genSecretKeySpec(password);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            IvParameterSpec ivp = new IvParameterSpec(ivParam.getBytes(CommonConst.ENCODING_UTF_8));//使用CBC模式，需要一个向量iv，可增加加密算法的强度
            cipher.init(Cipher.ENCRYPT_MODE, secSpec, ivp);
            byte[] encrypted = cipher.doFinal(content.getBytes(CommonConst.ENCODING_UTF_8));
            return HEXUtil.encode(CodeUtil.base64Encode(encrypted));
        }catch(Throwable e){
            throw new ApiException("AES加密异常", e);
        }
    }

    public static String decrypt(String content, String password, String ivParam){
        try {
            SecretKeySpec secSpec = genSecretKeySpec(password);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            IvParameterSpec ivp = new IvParameterSpec(ivParam.getBytes(CommonConst.ENCODING_UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, secSpec, ivp);
            byte[] encrypted1 = CodeUtil.base64Decode(HEXUtil.decode(content));
            byte[] original = cipher.doFinal(encrypted1);
            return new String(original, CommonConst.ENCODING_UTF_8);
        }catch(Throwable e){
            throw new ApiException("AES解密异常", e);
        }
    }

    /**
     * 生成密钥对象 密钥可支持16位或32位，如果是32位，可能会报：java.security.InvalidKeyException: Illegal key size 异常，此时需要更换JDK的local_policy.jar和US_export_policy.jar
     * @param password
     * @return
     * @throws Exception
     */
    public static SecretKeySpec genSecretKeySpec(String password) throws Exception{
        if (password == null || (password.length() != 16 && password.length() != 32)) {
            throw new ApiException("password长度须为16或32位");
        }
        KeyGenerator kGen = KeyGenerator.getInstance(Algorithm.AES);
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(password.getBytes());
        kGen.init(password.length()==16 ? 128 : 256, secureRandom);
        SecretKey secretKey = kGen.generateKey();
        byte[] enCodeFormat = secretKey.getEncoded();
        return new SecretKeySpec(enCodeFormat, Algorithm.AES);
    }
}
