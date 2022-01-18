package ru.gosuslugi.pgu.fs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Черновик не найден")
public class DraftNotFoundException extends FormBaseException {
    public DraftNotFoundException() {
        super("Черновик не найден");
    }

    public DraftNotFoundException(String msg) {
        super(msg);
    }
}
