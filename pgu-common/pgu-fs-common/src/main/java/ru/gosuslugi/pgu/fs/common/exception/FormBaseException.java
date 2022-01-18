package ru.gosuslugi.pgu.fs.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.gosuslugi.pgu.common.core.exception.PguException;

/**
 * Root of project exceptions
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FormBaseException extends PguException {

    public FormBaseException(String s) {
        super(s);
    }

    public FormBaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public FormBaseException(Throwable cause) {
        super(cause);
    }

}
