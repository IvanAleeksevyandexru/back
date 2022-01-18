package ru.gosuslugi.pgu.fs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;

/**
 * Запрос на услугу дублирует еще не завершенный запрос ываыва
 */
@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Дублирующий запрос на услугу запрещен")
public class DuplicateOrderException extends FormBaseException {
    public DuplicateOrderException() {
        super("Дублирующий запрос на услугу запрещен");
    }

    public DuplicateOrderException(String message){
        super(message);
    }
}
