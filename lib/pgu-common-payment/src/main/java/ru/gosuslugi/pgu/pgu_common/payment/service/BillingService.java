package ru.gosuslugi.pgu.pgu_common.payment.service;

import ru.gosuslugi.pgu.core.lk.model.payment.BillData;
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.BillInfoResponseWrapper;
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.ImportBillNewResponse;
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.ImportBillStatusResponse;
import ru.gosuslugi.pgu.pgu_common.payment.dto.pay.PaymentPossibilityRequest;
import ru.gosuslugi.pgu.pgu_common.payment.dto.pay.PaymentPossibilityResponse;

import java.util.Map;

public interface BillingService {
    BillInfoResponseWrapper getBillInfo(String token, String billId);

    ImportBillStatusResponse getBillStatus(String token, String requestId);

    ImportBillNewResponse getNewBillNumber(String token, Map<String, Object> parameters);

    String getBillPdfURI(String billId);

    PaymentPossibilityResponse getPaymentPossibleDecision(PaymentPossibilityRequest request);

    BillInfoResponseWrapper getBillInfoByBillNumber(String token, String billNumber);

    boolean isBillPaid(BillInfoResponseWrapper billInfoResponse);

    BillData setBillToOrder(String token, String serviceId, Long orderId, String billNumber);
}
