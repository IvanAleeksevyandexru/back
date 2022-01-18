package ru.gosuslugi.pgu.fs.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DuplicateValueForOrderFoundException extends FormBaseWorkflowException {

    public DuplicateValueForOrderFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
