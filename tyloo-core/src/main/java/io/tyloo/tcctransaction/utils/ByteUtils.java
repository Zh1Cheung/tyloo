package io.tyloo.tcctransaction.utils;

import org.apache.commons.lang3.math.NumberUtils;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:49 2019/12/4
 *
 */
public class ByteUtils {

    public static byte[] longToBytes(long num) {
        return String.valueOf(num).getBytes();
    }

    public static long bytesToLong(byte[] bytes) {
        return Long.valueOf(new String(bytes));
    }

    public static byte[] intToBytes(int num) {
        return String.valueOf(num).getBytes();
    }

    public static int bytesToInt(byte[] bytes) {
        return Integer.valueOf(new String(bytes));
    }

}
