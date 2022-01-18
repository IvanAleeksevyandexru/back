package ru.gosuslugi.pgu.fs.common.exception.dto.message;

import lombok.Data;
import org.springframework.http.HttpStatus;
import ru.gosuslugi.pgu.common.core.exception.dto.ModalComponentButton;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException;
import ru.gosuslugi.pgu.fs.common.exception.NoRightsForSendingApplicationException;
import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorContent;
import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorModalWindow;
import ru.gosuslugi.pgu.fs.common.exception.dto.StatusIcon;
import ru.gosuslugi.pgu.fs.common.exception.dto.error.ErrorMessageWithModal;

import java.util.List;


@Data
public class NoRightsForSendingApplicationError extends ErrorMessageWithModal {

    private static final String NO_RIGHTS_FOR_SENDING_APPLICATION = "NO_RIGHTS_FOR_SENDING_APPLICATION";

    public NoRightsForSendingApplicationError(NoRightsForSendingApplicationException ex) {
        this.status = NO_RIGHTS_FOR_SENDING_APPLICATION;
        this.message = HttpStatus.FORBIDDEN.getReasonPhrase();
        this.description = ex.getMessage();
    }

    @Override
    protected ErrorModalWindow createWindow(String reason, String message, FormBaseWorkflowException ex) {
        ErrorModalWindow errorModalWindow = new ErrorModalWindow();
        ErrorContent errorContent = new ErrorContent();
        errorContent.setHeader("Недостаточно прав для подачи заявления");
        errorContent.setHelperText("У вас недостаточно прав для подачи заявления.");
        errorContent.setStatusIcon(StatusIcon.warning);
        errorModalWindow.setContent(errorContent);
        ModalComponentButton closeButton = new ModalComponentButton();
        closeButton.setLabel("Вернуться к заявлению");
        closeButton.setCloseModal(true);
        errorModalWindow.setButtons(List.of(closeButton));
        return errorModalWindow;
    }
}
