package ru.gosuslugi.pgu.pgu_common.payment.dto.pay;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class PaymentPossibilityRequest {

    @EqualsAndHashCode.Include
    private List<String> amountCodes;
    @EqualsAndHashCode.Include
    private String applicantType;
    @EqualsAndHashCode.Include
    private String organizationId;
    @EqualsAndHashCode.Include
    private String serviceId;
    private String serviceCode;
    @EqualsAndHashCode.Include
    private Long orderId;
    private String token;
    private String billId;
    private String billNumber;
    private Map<String, Integer> fullAmountCodePrices;
    private Map<String, Integer> saleAmountCodePrices;
    private String orgRequisitesResponseVersion;

    /**
     * Params for retry bill service call
     */
    private Map<String, String> retryOrgRequisites;
    private Map<String, String> retryPayRequisites;
    private Integer retryPayFullAmount;
    private Integer retryPaySaleAmount;

    private String orgRequisitesDictionary;
    private String orgRequisitesDictionaryTx;
    private List<Map<String, String>> orgRequisitesFilters;
    private String payRequisitesDictionaryTx;
    private List<Map<String, String>> payRequisitesFilters;
    private String returnUrl;
    private String payerIdType;
    private String payerIdNum;

    public void addFullAmountCodePrice(String amountcode, Integer amountPrice) {
        fullAmountCodePrices.put(amountcode, amountPrice);
    }

    public void addSaleAmountCodePrice(String amountcode, Integer amountPrice) {
        saleAmountCodePrices.put(amountcode, amountPrice);
    }

    public void setDictionaryFilterValues(List<Map<String, String>> filterValues){
        for (Map<String, String> simpleMap : filterValues) {
            String value = simpleMap.get("value");
            value = value.equals("organizationId") ? organizationId :
                    value.equals("serviceExtId") ? serviceCode : value;
            simpleMap.put("value", value);
        }
    }

}
