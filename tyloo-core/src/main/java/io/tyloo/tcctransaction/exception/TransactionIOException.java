package io.tyloo.tcctransaction.exception;

/*
 *
 * 事务IO异常
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:27 2019/12/4
 *
 */
public class TransactionIOException extends RuntimeException {

    private static final long serialVersionUID = 6508064607297986329L;

    public TransactionIOException(String message) {
        super(message);
    }

    public TransactionIOException(Throwable e) {
        super(e);
    }
}
