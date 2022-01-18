package ru.gosuslugi.pgu.fs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;

@ResponseStatus(code = HttpStatus.GONE, reason = "Ошибка при создании заявления")
public class OrderCreationException extends FormBaseException {

    public OrderCreationException(String s) {
        super(s);
    }
}
