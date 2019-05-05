package io.tyloo.utils;

/*
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 19:49 2019/5/30
 *
 */
public class StringUtils {

    public static boolean isNotEmpty(String value) {

        if(value == null) {
            return false;
        }

        if(value.equals("")) {
            return false;
        }

        return true;
    }
}
