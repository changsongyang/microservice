package org.study.common.util.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /**
     *
     * @param str
     * @return
     */
    public static String getMD5Str(String str) {
        MessageDigest messageDigest = null;

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            if (!StringUtil.isEmpty(str)) {
                messageDigest.update(str.getBytes("UTF-8"));
            }
        } catch (NoSuchAlgorithmException e) {
            logger.error("getMD5Str异常", e);
        } catch (UnsupportedEncodingException e) {
            logger.error("getMD5Str异常", e);
        }

        byte[] byteArray = messageDigest.digest();

        StringBuffer md5StrBuff = new StringBuffer();

        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1){
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            } else {
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
            }
        }
        return md5StrBuff.toString().toUpperCase();
    }
}
