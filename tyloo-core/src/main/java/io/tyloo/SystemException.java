package io.tyloo;

/*
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 19:55 2019/6/1
 *
 */
public class SystemException extends RuntimeException {

    public SystemException(String message) {
        super(message);
    }

    public SystemException(Throwable e) {
        super(e);
    }

    public SystemException(String message, Throwable e) {
        super(message, e);
    }
}
