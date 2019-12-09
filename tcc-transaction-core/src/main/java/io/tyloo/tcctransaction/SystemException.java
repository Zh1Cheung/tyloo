package io.tyloo.tcctransaction;

/*
 *
 * 系统异常
 *
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:01 2019/12/4
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
