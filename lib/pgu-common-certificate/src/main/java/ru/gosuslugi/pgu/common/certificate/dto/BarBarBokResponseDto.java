package ru.gosuslugi.pgu.common.certificate.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.beans.ConstructorProperties;

/**
 * Класс запроса в сервис barbarbok
 * в поле data строка, содержащая сформированный xml запроса о сертификате дополнительного образования
 * см.  https://confluence.egovdev.ru/pages/viewpage.action?pageId=193302173
 */

@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor(onConstructor_ = {@ConstructorProperties({"id", "data", "ttl", "status", "errorMessage"})})
@JsonIgnoreProperties(ignoreUnknown = true)
public class BarBarBokResponseDto {
    String id;
    String data;
    Long ttl;
    String status;
    String errorMessage;
}
