package com.igeeksky.xcache.core.util;

/**
 * 哈希字符串工具类
 *
 * @author Patrick.Lau
 * @date 2017-01-11
 */
public class HexUtil {

    private static final char hexDigitsUC[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final char hexDigitsLC[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * byte[]转16进制字符串
     *
     * @param toLowerCase
     * @param digest
     * @return
     */
    public static String encodeHexString(boolean toLowerCase, byte[] digest) {
        //判断大小写
        char[] hexDigits = hexDigitsUC;
        if (toLowerCase) {
            hexDigits = hexDigitsLC;
        }
        //转换成16进制
        int length = digest.length;
        char[] chars = new char[length * 2];
        int k = 0;
        for (int i = 0; i < length; i++) {
            byte byte0 = digest[i];
            chars[k++] = hexDigits[byte0 >>> 0x4 & 0xf];
            chars[k++] = hexDigits[byte0 & 0xf];
        }
        return new String(chars);
    }

    private HexUtil() {
    }

}
