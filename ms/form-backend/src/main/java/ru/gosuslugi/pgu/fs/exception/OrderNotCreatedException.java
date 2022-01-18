package ru.gosuslugi.pgu.fs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;

@ResponseStatus(code = HttpStatus.GONE, reason = "Заявление не создано")
public class OrderNotCreatedException extends FormBaseException {

    public OrderNotCreatedException(String s) {
        super(s);
    }
}
