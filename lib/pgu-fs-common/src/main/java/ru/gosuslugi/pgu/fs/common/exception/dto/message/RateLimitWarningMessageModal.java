package ru.gosuslugi.pgu.fs.common.exception.dto.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.gosuslugi.pgu.common.core.exception.dto.ModalComponentButton;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException;
import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorContent;
import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorModalWindow;
import ru.gosuslugi.pgu.fs.common.exception.dto.StatusIcon;
import ru.gosuslugi.pgu.fs.common.exception.dto.error.ErrorMessageWithModal;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class RateLimitWarningMessageModal extends ErrorMessageWithModal {

    public RateLimitWarningMessageModal(String reason, String message, FormBaseWorkflowException ex) {
        super(reason, message, ex);
    }

    @Override
    protected ErrorModalWindow createWindow(String reason, String message, FormBaseWorkflowException ex) {
        ErrorModalWindow errorModalWindow = new ErrorModalWindow();
        ErrorContent errorContent = new ErrorContent();
        errorContent.setHeader(message);
        errorContent.setHelperText(reason);
        errorContent.setStatusIcon(StatusIcon.warning);
        errorModalWindow.setContent(errorContent);
        errorModalWindow.setCloseHandlerCase("redirectToLK");
        ModalComponentButton closeButton = new ModalComponentButton();
        closeButton.setLabel("Закрыть");
        closeButton.setCloseModal(true);
        errorModalWindow.setButtons(List.of(closeButton));
        return errorModalWindow;
    }
}
