package ru.gosuslugi.pgu.fs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;

/**
 * Ошибка парсинга JSON в валидации компонента выбора временного интервала
 */
@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Ошибка парсинга JSON в валидации компонента выбора временного интервала")
public class DtPeriodComponentIOException extends FormBaseException {
    public DtPeriodComponentIOException() {
        super("Ошибка парсинга JSON в валидации компонента выбора временного интервала");
    }
}
