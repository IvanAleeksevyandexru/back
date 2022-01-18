package ru.gosuslugi.pgu.fs.component.payment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.gosuslugi.pgu.pgu_common.payment.dto.pay.PaymentPossibilityResponse;

import java.util.List;

@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BillContainer {
    private List<String> amountCodes;
    private String organizationId;
    private Long orderId;
    private String serviceId;
    private String serviceCode;
    private PaymentPossibilityResponse paymentResponse;

    public boolean hasDifferences(BillContainer latestData) {
        if (amountCodes.size() != latestData.amountCodes.size()) return true;
        if (!amountCodes.containsAll(latestData.amountCodes)) return true;
        return !organizationId.equals(latestData.organizationId);
    }
}
