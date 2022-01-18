package ru.gosuslugi.pgu.fs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.gosuslugi.pgu.fs.common.exception.ErrorModalException;
import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorModalWindow;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "NO_RIGHTS_FOR_SENDING_APPLICATION")
public class NoEmpowermentException extends ErrorModalException {

    public NoEmpowermentException(ErrorModalWindow errorModal, String s) {
        super(errorModal, s);
    }

    public NoEmpowermentException(ErrorModalWindow modalWindow) {
        super(modalWindow, "Нет доверенности для прохождения услуги");
    }

    public NoEmpowermentException(String message){
        super(message);
    }
}
