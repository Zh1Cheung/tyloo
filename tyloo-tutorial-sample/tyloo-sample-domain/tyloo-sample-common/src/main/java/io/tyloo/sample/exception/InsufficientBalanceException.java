package io.tyloo.sample.exception;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:30 2019/12/5
 *
 */
public class InsufficientBalanceException extends RuntimeException {
    private static final long serialVersionUID = 6689953065473521009L;

    public InsufficientBalanceException() {

    }

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
