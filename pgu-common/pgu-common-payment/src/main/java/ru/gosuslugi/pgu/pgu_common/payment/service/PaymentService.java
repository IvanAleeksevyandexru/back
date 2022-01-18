package ru.gosuslugi.pgu.pgu_common.payment.service;

import ru.gosuslugi.pgu.pgu_common.payment.dto.PaymentInfo;
import ru.gosuslugi.pgu.pgu_common.payment.dto.PaymentStatusInfo;

import java.util.List;

public interface PaymentService {

    List<PaymentInfo> getUnusedPaymentsV1(Long orderId, String orgCode, String token, String serviceId, String passportTC, String registrationType, String grzYes);

    List<PaymentInfo> getUnusedPaymentsV3(Long orderId, String orgCode, String token, String serviceId, String applicantType, Long amount);

    /**
     * Получает статус платежа из сервиса оплаты
     * @param orderId идентификатор черновика
     * @param token токен пользователя
     * @param payCode код заявителя (1 - для первого, 2 - для второго)
     * @return статус платежа
     */
    PaymentStatusInfo getPaymentStatus(Long orderId, String token, String payCode, String billId);
}
