package ru.gosuslugi.pgu.fs.common.exception;

import ru.gosuslugi.pgu.common.core.exception.PguQuietException;

/**
 * Ошибки, унаследованные от этого класса, считают штатным выполнением программы,
 * и логгируются с уровнем INFO и без стэктрейса.
 */
public class FormBaseWorkflowException extends PguQuietException {

    public FormBaseWorkflowException(String s) {
        super(s);
    }

    public FormBaseWorkflowException(String message, Throwable cause) {
        super(message, cause);
    }

}
