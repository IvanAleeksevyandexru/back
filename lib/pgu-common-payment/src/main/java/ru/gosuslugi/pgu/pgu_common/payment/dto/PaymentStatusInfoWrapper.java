package ru.gosuslugi.pgu.pgu_common.payment.dto;

import lombok.Data;

import java.util.List;

@Data
public class PaymentStatusInfoWrapper {

    List<PaymentStatusInfo> paymentStatus;

}
