package ru.gosuslugi.pgu.fs.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.gosuslugi.pgu.common.core.exception.PguException;

/**
 * Ошибка проверки условий для перехода
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ConditionCheckerException extends PguException {

    public ConditionCheckerException(String s) {
        super(s);
    }

    public ConditionCheckerException(String message, Throwable cause) {
        super(message, cause);
    }
}
