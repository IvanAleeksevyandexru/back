package ru.gosuslugi.pgu.fs.common.exception.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.gosuslugi.pgu.common.core.exception.dto.error.ErrorMessage;
import ru.gosuslugi.pgu.common.core.exception.handler.ExceptionHandlerHelper;
import ru.gosuslugi.pgu.fs.common.exception.DuplicateValueForOrderFoundException;
import ru.gosuslugi.pgu.fs.common.exception.ErrorModalException;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException;
import ru.gosuslugi.pgu.fs.common.exception.NoRightsForSendingApplicationException;
import ru.gosuslugi.pgu.fs.common.exception.dto.message.DefaultErrorMessageWithModal;
import ru.gosuslugi.pgu.fs.common.exception.dto.message.DuplicateValueForOrderFoundError;
import ru.gosuslugi.pgu.fs.common.exception.dto.message.NoRightsForSendingApplicationError;
import ru.gosuslugi.pgu.fs.common.exception.dto.message.RateLimitWarningMessageModal;
import ru.gosuslugi.pgu.common.core.exception.RateLimitServiceException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class FormCommonExceptionHandler {

    private final ExceptionHandlerHelper exceptionHandlerHelper;

    @ExceptionHandler(ErrorModalException.class)
    public ResponseEntity<? extends ErrorMessage> handleException(ErrorModalException ex) {
        return exceptionHandlerHelper.handleModal(ex, (reason, status) -> new DefaultErrorMessageWithModal(reason, status, ex));
    }

    @ExceptionHandler(FormBaseWorkflowException.class)
    public ResponseEntity<? extends ErrorMessage> handleException(FormBaseWorkflowException ex) {
        return exceptionHandlerHelper.handle(ex, (reason, status) -> new DefaultErrorMessageWithModal(reason, status, ex));
    }

    @ExceptionHandler(DuplicateValueForOrderFoundException.class)
    public ResponseEntity<? extends ErrorMessage> handleException(DuplicateValueForOrderFoundException ex) {
        return exceptionHandlerHelper.handle(ex, (reason, status) -> new DuplicateValueForOrderFoundError(ex));
    }

    @ExceptionHandler(NoRightsForSendingApplicationException.class)
    public ResponseEntity<ErrorMessage> handleException(NoRightsForSendingApplicationException ex) {
        return exceptionHandlerHelper.handle(ex, (reason, status) -> new NoRightsForSendingApplicationError(ex));
    }

    @ExceptionHandler(RateLimitServiceException.class)
    public ResponseEntity<ErrorMessage> handleException(RateLimitServiceException ex) {
        return exceptionHandlerHelper.handle(ex, (reason, status) ->
                new RateLimitWarningMessageModal(ex.getReason(), ex.getMessage(), new FormBaseWorkflowException(ex.getMessage())));
    }
}
