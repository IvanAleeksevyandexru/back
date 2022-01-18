package ru.gosuslugi.pgu.fs.common.exception.dto.message;

import lombok.Data;
import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorContent;
import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorModalWindow;
import ru.gosuslugi.pgu.common.core.exception.dto.ModalComponentButton;
import ru.gosuslugi.pgu.fs.common.exception.dto.StatusIcon;
import ru.gosuslugi.pgu.fs.common.exception.ErrorModalException;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException;
import ru.gosuslugi.pgu.fs.common.exception.dto.error.ErrorMessageWithModal;

import java.util.List;

@Data
public class DefaultErrorMessageWithModal extends ErrorMessageWithModal {

    public DefaultErrorMessageWithModal(String status, String message, ErrorModalException ex) {
        super(status, message, ex);
    }

    public DefaultErrorMessageWithModal(String reason, String message, FormBaseWorkflowException ex) {
        super(reason, message, ex);
    }

    @Override
    protected ErrorModalWindow createWindow(String reason, String message, FormBaseWorkflowException ex) {
        ErrorModalWindow errorModalWindow = new ErrorModalWindow();
        ErrorContent content = new ErrorContent();
        content.setHeader(reason);
        content.setHelperText(ex.getMessage());
        content.setStatusIcon(StatusIcon.warning);
        errorModalWindow.setContent(content);
        ModalComponentButton closeButton = new ModalComponentButton();
        closeButton.setLabel("Закрыть");
        closeButton.setCloseModal(true);
        errorModalWindow.setButtons(List.of(closeButton));
        return errorModalWindow;
    }
}
