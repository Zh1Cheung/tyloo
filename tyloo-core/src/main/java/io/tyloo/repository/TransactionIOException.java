package io.tyloo.repository;

/*
 *
 *  ¬ŒÒIO“Ï≥£
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 19:27 2019/5/16
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
