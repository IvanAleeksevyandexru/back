package ru.gosuslugi.pgu.pgu_common.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Информация о платежах
 */
@Data
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentsInfo {
    List<PaymentInfo> payment;
}
