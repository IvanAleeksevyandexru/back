package ru.gosuslugi.pgu.fs.component.dictionary.dto.kindergarten;

import ru.gosuslugi.pgu.common.kindergarten.xsd.dto.FormData;

/**
 * Тип запроса, отправляемый в xml для получения данных по детским садам.
 */
public enum RequestType {
    /**
     * Заполняет объект {@link FormData} с помощью {@link ru.gosuslugi.pgu.common.kindergarten.xsd.dto.GetApplicationRequestType}
     */
    edit,
    /**
     * Заполняет объект {@link FormData} с помощью {@link ru.gosuslugi.pgu.common.kindergarten.xsd.dto.GetApplicationAdmissionRequestType}
     */
    admission
}
