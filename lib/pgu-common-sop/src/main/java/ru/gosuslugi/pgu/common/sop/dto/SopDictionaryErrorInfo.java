package ru.gosuslugi.pgu.common.sop.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Информация об ошибке
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SopDictionaryErrorInfo {
    /** Код ошибки */
    private Integer code;
    /** Сообщение */
    private String message;
}
