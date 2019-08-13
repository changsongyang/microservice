package org.study.common.util.utils;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.study.common.statics.exceptions.BizException;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @Description:
 * @author: chenyf
 * @Date: 2018/1/5
 */
public class MD5Util {
    private static Logger logger = LoggerFactory.getLogger(MD5Util.class);

    public static String getMD5Hex(String str) {
        return String.valueOf(Hex.encodeHex(getMD5(str), true));
    }

    /**
     * @param str
     * @return
     */
    public static byte[] getMD5(String str) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            if (StringUtil.isNotEmpty(str)) {
                messageDigest.update(str.getBytes("UTF-8"));
            }
        } catch (NoSuchAlgorithmException e) {
            throw new BizException("生成MD5信息时异常", e);
        } catch (UnsupportedEncodingException e) {
            throw new BizException("生成MD5信息时异常", e);
        }
        return messageDigest.digest();
    }


}
