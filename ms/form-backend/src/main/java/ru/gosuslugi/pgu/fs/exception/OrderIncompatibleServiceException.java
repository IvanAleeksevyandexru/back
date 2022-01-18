package ru.gosuslugi.pgu.fs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.gosuslugi.pgu.fs.common.exception.ErrorModalException;
import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorModalWindow;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Заявление не совместимо с услугой")
public class OrderIncompatibleServiceException extends ErrorModalException {

    public OrderIncompatibleServiceException(ErrorModalWindow modalWindow) {
        super(modalWindow, "Заявление не совместимо с услугой");
    }
}
