package ru.gosuslugi.pgu.fs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.gosuslugi.pgu.common.core.exception.dto.ModalComponentButton;
import ru.gosuslugi.pgu.common.core.exception.dto.ModalComponentButtonAction;
import ru.gosuslugi.pgu.fs.common.exception.ErrorModalException;
import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorContent;
import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorModalWindow;
import ru.gosuslugi.pgu.fs.common.exception.dto.StatusIcon;

import java.util.List;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "NOT_EDITABLE_STATUS")
public class DraftNotEditableException  extends ErrorModalException {

    public DraftNotEditableException(ErrorModalWindow errorModal, String s) {
        super(errorModal, s);
    }

    public DraftNotEditableException(ErrorModalWindow modalWindow) {
        super(modalWindow, "Заявка находится в запрещённом для редактирования статусе");
    }

    public DraftNotEditableException(String message){
        super(message);
    }

    public static ErrorModalWindow createWindow(String serviceName, boolean plural) {
        ErrorModalWindow errorModalWindow = new ErrorModalWindow();
        errorModalWindow.setShowCloseButton(false);
        errorModalWindow.setShowCrossButton(false);
        errorModalWindow.setHideTraceId(true);
        ErrorContent content = new ErrorContent();
        content.setHeader("Это заявление уже отправлено");
        if(plural){
            content.setHelperText("Статус заявления можно проверить в личном кабинете.<br/>Услуга: " + serviceName);
        } else {
            content.setHelperText("Статус заявления можно проверить в личном кабинете. " +
                    "Если хотите отправить новое заявление, " +
                    "сначала дождитесь решения по уже отправленному или отмените его.<br/>Услуга: " + serviceName);
        }
        content.setStatusIcon(StatusIcon.warning);
        errorModalWindow.setContent(content);
        ModalComponentButton closeButton = new ModalComponentButton();
        closeButton.setLabel("В личный кабинет");
        closeButton.setValue("В личный кабинет");
        closeButton.setCloseModal(false);
        ModalComponentButtonAction closeButtonAction = new ModalComponentButtonAction();
        closeButtonAction.setType("redirectToLK");
        closeButtonAction.setValue("getNextScreen");
        closeButton.setAction(closeButtonAction);

        ModalComponentButton newButton = new ModalComponentButton();
        newButton.setLabel("Заполнить новое заявление");
        newButton.setValue("Заполнить новое заявление");
        newButton.setCloseModal(true);

        errorModalWindow.setButtons(List.of(newButton, closeButton));
        return errorModalWindow;
    }

}
