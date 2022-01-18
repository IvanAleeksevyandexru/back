package ru.gosuslugi.pgu.fs.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.fs.service.ErrorScreenService;

/**
 * Подключение внешнего обработчика ошибок.
 * Можно добавить обработчики, специфичные для данного модуля.
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class FormServiceExceptionHandler {

    private final ErrorScreenService errorScreenService;

    @ExceptionHandler(ErrorScreenException.class)
    public ScenarioResponse handleException(ErrorScreenException e) {
        return errorScreenService.getInitScreen(e.getErrorCode(), e.getExternalErrorInfo());
    }
}
