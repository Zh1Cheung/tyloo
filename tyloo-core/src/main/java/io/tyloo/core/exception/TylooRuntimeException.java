package io.tyloo.core.exception;

public class TylooRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -1949770547060521702L;

    /**
     * Instantiates a new Tcc runtime exception.
     */
    public TylooRuntimeException() {
    }

    /**
     * Instantiates a new Tcc runtime exception.
     *
     * @param message the message
     */
    public TylooRuntimeException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new Tcc runtime exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public TylooRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Tcc runtime exception.
     *
     * @param cause the cause
     */
    public TylooRuntimeException(final Throwable cause) {
        super(cause);
    }
}
