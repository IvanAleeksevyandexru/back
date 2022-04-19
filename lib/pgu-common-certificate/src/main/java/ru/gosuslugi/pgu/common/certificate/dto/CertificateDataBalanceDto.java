package ru.gosuslugi.pgu.common.certificate.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.beans.ConstructorProperties;

/**
 * Сведения о балансе сертификата
 */
@Value
@AllArgsConstructor(onConstructor_ = {@ConstructorProperties({"certificateBalance", "certificateBookedAmount"})})
public class CertificateDataBalanceDto {
    /**
     * Баланс сертификата
     */
    double certificateBalance;
    /**
     * Забронированая часть сертификата (недоступно к использованию)
     */
    double certificateBookedAmount;

    double certificateAvailableBalance = 0.0D;

    //    Прошу в массивы pfdo, valued, preprof, other добавить certificateAvailableBalance как разницу между certificateBalance и certificateBookedAmount
    public double getCertificateAvailableBalance() {
        return certificateBalance - certificateBookedAmount;
    }


}
