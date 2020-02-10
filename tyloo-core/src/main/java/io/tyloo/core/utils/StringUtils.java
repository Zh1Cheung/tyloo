package io.tyloo.core.utils;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:50 2019/12/4
 *
 */
public class StringUtils {

    public static boolean isNotEmpty(String value) {

        if(value == null) {
            return false;
        }

        return !value.equals("");

    }
}
