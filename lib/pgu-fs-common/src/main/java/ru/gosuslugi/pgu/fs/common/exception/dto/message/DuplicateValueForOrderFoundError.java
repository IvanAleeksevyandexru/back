package ru.gosuslugi.pgu.fs.common.exception.dto.message;

import lombok.Data;
import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorContent;
import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorModalWindow;
import ru.gosuslugi.pgu.common.core.exception.dto.ModalComponentButton;
import ru.gosuslugi.pgu.fs.common.exception.dto.StatusIcon;
import ru.gosuslugi.pgu.fs.common.exception.DuplicateValueForOrderFoundException;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException;
import ru.gosuslugi.pgu.fs.common.exception.dto.error.ErrorMessageWithModal;

import java.util.List;

@Data
public class DuplicateValueForOrderFoundError extends ErrorMessageWithModal {

    private static String DUPLICATE_ORDER_STATUS = "DUPLICATE_ORDER";
    private static String DUPLICATE_ORDER_MESSAGE = "Ошибка при проверке дубликатов заявления";

    public DuplicateValueForOrderFoundError(DuplicateValueForOrderFoundException ex) {
        super(DUPLICATE_ORDER_STATUS, DUPLICATE_ORDER_MESSAGE, ex);
    }

    @Override
    protected ErrorModalWindow createWindow(String reason, String message, FormBaseWorkflowException ex) {
        ErrorModalWindow errorModalWindow = new ErrorModalWindow();
        ErrorContent errorContent = new ErrorContent();
        errorContent.setHeader("Подать заявление пока нельзя");
        errorContent.setHelperText("У вас уже есть поданное заявление по этой услуге. Чтобы получить новое — дождитесь результатов обработки текущего.");
        errorContent.setStatusIcon(StatusIcon.warning);
        errorModalWindow.setContent(errorContent);
        ModalComponentButton closeButton = new ModalComponentButton();
        closeButton.setLabel("Вернуться к заявлению");
        closeButton.setCloseModal(true);
        errorModalWindow.setButtons(List.of(closeButton));
        return errorModalWindow;
    }
}
