package ru.gosuslugi.pgu.common.certificate.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.beans.ConstructorProperties;

/**
 * Причины отказа в создании/выпуске сертификата
 */
@Value
@AllArgsConstructor(onConstructor_ = {@ConstructorProperties({"cancelReasonCode", "cancelReasonDescription"})})
public class CertificateCancelResponseDto {
    /**
     * Код причины отказа в создании/выпуске сертификата
     */
    Long cancelReasonCode;
    /**
     * Описание причины отказа в создании/выпуске сертификата
     */
    String cancelReasonDescription;
}
