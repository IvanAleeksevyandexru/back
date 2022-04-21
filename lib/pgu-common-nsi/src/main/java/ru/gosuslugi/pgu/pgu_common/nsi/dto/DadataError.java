package ru.gosuslugi.pgu.pgu_common.nsi.dto;

import lombok.Data;

/**
 * Ошибка сервиса Dadata
 */
@Data
public class DadataError {

    /**
     * Код результата обращения на прямой сервис Дадаты:
     *
     * 0 - значит запрос к сервису dadata был выполнен успешно и
     * в модуле nsi была осуществлена успешная обработка ответа от dadata;
     * 500 - ошибка
     */
    private Integer code;

    /**
     * Описание кода
     */
    private String message;
}
