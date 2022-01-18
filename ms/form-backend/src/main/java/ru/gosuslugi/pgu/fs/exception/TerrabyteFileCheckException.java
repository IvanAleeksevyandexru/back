package ru.gosuslugi.pgu.fs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.gosuslugi.pgu.fs.common.exception.ErrorModalException;
import ru.gosuslugi.pgu.fs.common.exception.dto.ErrorModalWindow;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Ошибка при проверке метаданных по загруженным файлам")
public class TerrabyteFileCheckException extends ErrorModalException {

    public TerrabyteFileCheckException(ErrorModalWindow errorModal, String s) {
        super(errorModal, s);
    }
}
