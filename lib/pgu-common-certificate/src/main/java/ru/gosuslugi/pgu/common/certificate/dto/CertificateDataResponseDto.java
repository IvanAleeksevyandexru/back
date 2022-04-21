package ru.gosuslugi.pgu.common.certificate.dto;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.beans.ConstructorProperties;

/**
 * https://confluence.egovdev.ru/pages/viewpage.action?pageId=193302454
 * Ответ на запрос сертификата, возвращаемый из компонента
 * CertificateEaisdoComponent.getInitialValue
 */
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor(onConstructor_ = {
        @ConstructorProperties({"certificateGUID",
                "certificateNumber", "certificateType",
                "certificateCategoryCode",
                "certificateCategoryName",
                "pfdo", "valued", "preprof", "other",
                "cancelResponse",
                "isReleasedOnRequestByEPGU", "isTimeout"})
})
public class CertificateDataResponseDto {
    /**
     * Глобально уникальный идентификатор сертификата
     */
    String certificateGUID;
    /**
     * Номер сертификата в навигаторе
     */
    String certificateNumber;
    /**
     * Тип сертификата: 0 - Сертификат дополнительного образования; 1 - Сертификат перс. финансирования
     */
    Long certificateType; // 0,
    Long certificateCategoryCode; // 0,
    String certificateCategoryName; // "От 5 до 17 лет"
    /**
     * Сведения о балансе сертификата персонифицированного финансирования
     * (сумма денежных средств, доступная к использованию для записи на программы ДО
     * из реестра программ персонифицированного финансирования)
     * и сумме денежных средств, недоступной для использования (забронированной)
     */
    CertificateDataBalanceDto pfdo;
    /**
     * Сведения о балансе сертификата (количество программ ДО из реестра значимых программ, на которое может записаться держатель сертификата) и количество программ из указанного реестра, уже реализуемых по данному сертификату
     */
    CertificateDataBalanceDto valued;
    /**
     * Сведения о балансе сертификата
     * (количество программ ДО из реестра предпрофессиональных программ,
     * на которое может записаться держатель сертификата)
     * и количество программ из указанного реестра, уже реализуемых по данному сертификату
     */
    CertificateDataBalanceDto preprof;
    /**
     * Сведения о балансе сертификата
     * (количество программ ДО из реестра иных программ,
     * на которое может записаться держатель сертификата)
     * и количество программ из указанного реестра, уже реализуемых по данному сертификату
     */
    CertificateDataBalanceDto other;
    CertificateCancelResponseDto cancelResponse;
    /**
     * Ответ на вопрос
     * "Сертификат выпущен в ответ на запрос, поступивший с ЕПГУ,
     * или был найден сертификат, ранее выпущенный региональным навигатором?".
     * Если значение элемента - true, то сертификат выпущен в ответ на запрос, поступивший с ЕПГУ.
     * Если значение элемента - false, значит данный сертификат был ранее выпущен в региональном навигаторе.
     */
    boolean isReleasedOnRequestByEPGU;

    boolean isTimeout;
}
