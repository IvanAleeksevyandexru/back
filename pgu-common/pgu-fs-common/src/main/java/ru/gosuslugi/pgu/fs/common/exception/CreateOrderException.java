package ru.gosuslugi.pgu.fs.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorModalWindow;


@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Невозможно создать заявление")
public class CreateOrderException extends ErrorModalException {
    public CreateOrderException(ErrorModalWindow errorModal, String s) {
        super(errorModal, s);
    }
}
