package ru.gosuslugi.pgu.fs.exception;

import lombok.Getter;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.common.core.exception.dto.ExternalErrorInfo;

@Getter
public class ErrorScreenException extends FormBaseException {

    public static final int NO_EMPOWERMENT_CODE = 1001;

    private final int errorCode;
    private final ExternalErrorInfo externalErrorInfo;


    public ErrorScreenException(int errorCode, ExternalErrorInfo externalErrorInfo) {
        super(externalErrorInfo.getMessage());
        this.errorCode = errorCode;
        this.externalErrorInfo = externalErrorInfo;
    }
}
