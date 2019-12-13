package io.tyloo.tcctransaction.exception;

/*
 *
 * 取消异常.
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:55 2019/12/4
 *
 */
public class CancellingException extends RuntimeException {


    public CancellingException(Throwable cause) {
        super(cause);
    }
}
