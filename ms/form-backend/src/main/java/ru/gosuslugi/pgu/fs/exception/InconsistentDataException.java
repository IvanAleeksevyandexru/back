package ru.gosuslugi.pgu.fs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Неконсистентные данные по услуге")
public class InconsistentDataException extends FormBaseException {
    public InconsistentDataException(String s) {
        super(s);
    }
}
