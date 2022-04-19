package ru.gosuslugi.pgu.pgu_common.payment.dto.pay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentPossibilityResponse {
    private String applicantType;
    private String billId;
    private String billNumber;
    private PaymentPossibilityRequestState state;
    private String errorMessage;

    private OrganizationRequisites organizationRequisites;
    private PaymentRequisites paymentRequisites;
    private String requestFullAmount;
    private String requestSaleAmount;

    public enum PaymentPossibilityRequestState {
        SUCCESS, REQUSITE_ERROR, SERVICE_ERROR, BILL_PAID
    }
}
