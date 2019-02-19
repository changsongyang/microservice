package org.study.common.util.utils;

import org.study.common.statics.exceptions.BizException;

/**
 * 16进制工具类
 * @author chenyf
 * @date 2018-12-15
 */
public class HEXUtil {
    private static final char[] DIGITS_LOWER =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static final char[] DIGITS_UPPER =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};


    public static String encode(String str){
        try{
            return encode(str.getBytes("UTF-8"), true);
        }catch (Exception e){
            throw new BizException("16进制转换失败", e);
        }
    }

    public static String encode(byte[] data, final boolean toUpperCase){
        return bytes2Hex(data, toUpperCase ? DIGITS_UPPER : DIGITS_LOWER);
    }

    public static String decode(String str){
        try{
            byte[] date = hex2Bytes(str);
            return new String(date, "UTF-8");
        }catch (Exception e){
            throw new BizException("16进制转换失败", e);
        }
    }

    private static String bytes2Hex(final byte[] data, final char[] toDigits) {
        final int l = data.length;
        final char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return new String(out);
    }

    private static byte[] hex2Bytes(final String data) throws Exception {
        final int len = data.length();

        if ((len & 0x01) != 0) {
            throw new Exception("Odd number of characters.");
        }

        final byte[] out = new byte[len >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data.charAt(j), j) << 4;
            j++;
            f = f | toDigit(data.charAt(j), j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }
        return out;
    }

    /**
     * 16转化为数字
     * @param ch 16进制
     * @param index 索引
     * @return 转化结果
     * @throws Exception 转化失败异常
     */
    private static int toDigit(final char ch, final int index) throws Exception {
        final int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new Exception("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }


    public static void main(String[] args){
        String str = "都是的撒是都是23231221FD34334G F，；,;'@12^===+--/][\\{}";
        String strEncode = encode(str);
        String strDecode = decode(strEncode);

        System.out.println("strEncode = "+strEncode);
        System.out.println("strDecode = "+strDecode);
        System.out.println("str.equals(strDecode) ? " + (str.equals(strDecode)));
    }
}
