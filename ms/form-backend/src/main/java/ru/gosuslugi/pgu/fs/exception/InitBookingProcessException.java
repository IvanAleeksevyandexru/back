package ru.gosuslugi.pgu.fs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Ошибка во время инициализации формы записи на прием")
public class InitBookingProcessException extends FormBaseException {
    public InitBookingProcessException(String s) {
        super(s);
    }
}
