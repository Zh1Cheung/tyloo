package io.tyloo.tcctransaction.exception;

/*
 *
 * 当前事务异常
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:59 2019/12/4
 *
 */
public class ConcurrentTransactionException extends RuntimeException {
    private static final long serialVersionUID = 4099060614687283527L;

    public ConcurrentTransactionException() {

    }

    public ConcurrentTransactionException(String message) {
        super(message);
    }

    public ConcurrentTransactionException(String message, Throwable e) {
        super(message, e);
    }
}
