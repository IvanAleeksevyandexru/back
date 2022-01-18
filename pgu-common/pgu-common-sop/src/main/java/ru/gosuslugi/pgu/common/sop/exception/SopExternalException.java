package ru.gosuslugi.pgu.common.sop.exception;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.gosuslugi.pgu.common.core.exception.PguException;
import ru.gosuslugi.pgu.common.core.exception.dto.ExternalErrorInfo;

/**
 * Ошибка обращения к внешнему справочнику
 *   Примеры: отсутствие значений в справочнике, ошибка в теле от внешней системы с HTTP Code 200 и т.д.
 */
@ResponseStatus(code = HttpStatus.VARIANT_ALSO_NEGOTIATES, reason = "Ошибка обращения к внешнему справочнику")
public class SopExternalException extends PguException {

    /** Мета информация */
    private final ExternalErrorInfo value;


    public SopExternalException(String id, String url, HttpMethod method, String message, String okato) {
        super(id);
        this.value = new ExternalErrorInfo(id, url, method, message, okato);
    }

    public SopExternalException(String id, String url, HttpMethod method, String message, String okato, Throwable cause) {
        super(id, cause);
        this.value = new ExternalErrorInfo(id, url, method, message, okato);
    }

    @Override
    public ExternalErrorInfo getValue() {
        return this.value;
    }
}
