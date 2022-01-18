package ru.gosuslugi.pgu.fs.common.exception.dto.message;

import lombok.Data;
import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorContent;
import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorModalWindow;
import ru.gosuslugi.pgu.common.core.exception.dto.ModalComponentButton;
import ru.gosuslugi.pgu.fs.common.exception.dto.StatusIcon;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException;
import ru.gosuslugi.pgu.fs.common.exception.dto.error.ErrorMessageWithModal;

import java.util.List;

@Data
public class NoRightToCreateOrderError extends ErrorMessageWithModal {

    private static final String NO_RIGHTS_TO_CREATE_ORDER = "NoRightToCreateOrder";

    public NoRightToCreateOrderError(FormBaseException ex) {
        super();
        this.status = NO_RIGHTS_TO_CREATE_ORDER;
        this.description = ex.getMessage();
        this.value = ex.getValue();
    }

    @Override
    protected ErrorModalWindow createWindow(String reason, String message, FormBaseWorkflowException ex) {
        ErrorModalWindow errorModalWindow = new ErrorModalWindow();
        ErrorContent errorContent = new ErrorContent();
        errorContent.setHeader("Недостаточно прав для создания черновика заявления");
        errorContent.setHelperText("У вас недостаточно прав для создания черновика заявления.");
        errorContent.setStatusIcon(StatusIcon.warning);
        errorModalWindow.setContent(errorContent);
        ModalComponentButton closeButton = new ModalComponentButton();
        closeButton.setLabel("Вернуться к заявлению");
        closeButton.setCloseModal(true);
        errorModalWindow.setButtons(List.of(closeButton));
        return errorModalWindow;
    }
}
