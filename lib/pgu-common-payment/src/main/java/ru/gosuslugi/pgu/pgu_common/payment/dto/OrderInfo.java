package ru.gosuslugi.pgu.pgu_common.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Иноформация о заказе услуги
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderInfo {
    String orderDate;
    String linkToOrderForm;
    List<PaymentInfo> orderPayments;
}
