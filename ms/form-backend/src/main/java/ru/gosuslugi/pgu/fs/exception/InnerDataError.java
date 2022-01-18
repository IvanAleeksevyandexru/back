package ru.gosuslugi.pgu.fs.exception;

/**
 * Ошибки во внутренних структурах данных
 */
public class InnerDataError extends RuntimeException {

    public InnerDataError(String message) {
        super(message);
    }

    public InnerDataError(String message, Throwable cause) {
        super(message, cause);
    }
}
