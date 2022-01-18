package ru.gosuslugi.pgu.fs.common.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorContent;
import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorModalWindow;
import ru.gosuslugi.pgu.fs.common.exception.dto.StatusIcon;

@Getter
@Setter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ErrorModalException extends FormBaseException {
    ErrorModalWindow errorModal;

    public ErrorModalException(ErrorModalWindow errorModal, Throwable cause) {
        super(cause);
        this.errorModal = errorModal;
    }

    public ErrorModalException(ErrorModalWindow errorModal, String s) {
        super(s);
        this.errorModal = errorModal;
    }

    public ErrorModalException(Throwable cause, ErrorModalWindow errorModal) {
        super(cause);
        this.errorModal = errorModal;
    }

    public ErrorModalException(String s) {
        super(s);
    }

    public static ErrorModalException getWindowWithErrorIcon(String message, String submessage) {
        ErrorModalWindow modalWindow = new ErrorModalWindow();
        ErrorContent errorContent = new ErrorContent();
        errorContent.setHeader(message);
        errorContent.setHelperText(submessage);
        errorContent.setStatusIcon(StatusIcon.error);
        modalWindow.setContent(errorContent);
        return new ErrorModalException(modalWindow, message);
    }
}
