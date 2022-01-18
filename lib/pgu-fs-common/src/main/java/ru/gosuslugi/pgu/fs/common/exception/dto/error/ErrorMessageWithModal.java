package ru.gosuslugi.pgu.fs.common.exception.dto.error;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorModalWindow;
import ru.gosuslugi.pgu.common.core.exception.dto.error.ErrorMessage;
import ru.gosuslugi.pgu.fs.common.exception.ErrorModalException;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ErrorMessageWithModal extends ErrorMessage {

    protected ErrorModalWindow errorModalWindow;

    public ErrorMessageWithModal(String status, String message, ErrorModalException ex) {
        this.status = status;
        this.message = message;
        this.description = ex.getMessage();
        this.value = ex.getValue();
        this.errorModalWindow = ex.getErrorModal();
    }

    public ErrorMessageWithModal(String reason, String message, FormBaseWorkflowException ex) {
        super(reason, message, ex);
        this.errorModalWindow = createWindow(reason, message, ex);
    }

    protected abstract ErrorModalWindow createWindow(String reason, String message, FormBaseWorkflowException ex);
}
