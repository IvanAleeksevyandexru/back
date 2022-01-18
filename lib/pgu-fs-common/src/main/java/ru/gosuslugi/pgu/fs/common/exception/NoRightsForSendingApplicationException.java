package ru.gosuslugi.pgu.fs.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "Недостаточно прав для подачи заявления")
public class NoRightsForSendingApplicationException extends RuntimeException {

    public NoRightsForSendingApplicationException(String message) {
        super(message);
    }

    public NoRightsForSendingApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
