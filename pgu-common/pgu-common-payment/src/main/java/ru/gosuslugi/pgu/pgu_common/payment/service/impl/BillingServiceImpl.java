package ru.gosuslugi.pgu.pgu_common.payment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.gosuslugi.pgu.common.core.exception.EntityNotFoundException;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.common.core.exception.PguException;
import ru.gosuslugi.pgu.core.lk.model.payment.BillData;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionary;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionaryItem;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilter;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterRequest;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterSimple;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterSimpleValue;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterUnion;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryUnionType;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiSimpleDictionaryFilterContainer;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiUnionDictionaryFilterContainer;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService;
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.BillInfo;
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.BillInfoResponseWrapper;
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.ImportBillNewResponse;
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.ImportBillStatusResponse;
import ru.gosuslugi.pgu.pgu_common.payment.dto.pay.OrganizationRequisites;
import ru.gosuslugi.pgu.pgu_common.payment.dto.pay.PaymentPossibilityRequest;
import ru.gosuslugi.pgu.pgu_common.payment.dto.pay.PaymentPossibilityResponse;
import ru.gosuslugi.pgu.pgu_common.payment.dto.pay.PaymentRequisites;
import ru.gosuslugi.pgu.pgu_common.payment.mapper.DictionaryResponseMapper;
import ru.gosuslugi.pgu.pgu_common.payment.mapper.NameValueContainer;
import ru.gosuslugi.pgu.pgu_common.payment.service.BillingService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private static final String BILL_INFO_URL = "api/pay/v1/bills?billIds={billIds}&ci={ci}";
    private static final String BILL_INFO_URL_BY_BILL_NUMBER = "api/pay/v1/bills?billNumber={billNumber}&ci={ci}";
    private static final String IMPORT_BILL_NEW_URL = "api/pay/v1/import/bill/new";
    private static final String IMPORT_BILL_STATUS_URL = "api/pay/v1/import/bill/status?requestId={requestId}";
    private static final String BILL_PDF_URL = "api/pay/v1/bill/get/pdf?billIds={billdId}";
    private static final String BILL_PDF_URL_EXT = "api/pay/v1/bill/get/pdf";
    private static final String BILL_IDS_PARAM = "billIds";
    private static final String CI_PARAM = "ci";
    private static final String PAYGATE_BETA_REQ_API = "%sapi/lk/v1/paygate/beta/req/bill?orderId={orderId}&personalType={type}";
    private static final String SET_BILL_TO_ORDER_URL = "api/lk/v1/paygate/setBillDataToOrder?orderId={orderId}&billNumber={billNumber}";

    public static final String REQUISITE_STATE_DUTY = "REQUISITE_STATE_DUTY";
    public static final String STATE_DUTY = "STATE_DUTY";
    /**  Единица перевода суммы платежа. ( 1р = 100копеек ) */
    public static final int CURRENCY_MULTIPLY_UNIT = 100;
    public static final String ZERO_AMOUNT_VALUE = "0";

    @Value("${pgu.payment-url}")
    private String pguUrl;

    @Value("${pgu.payment-ipsh-url}")
    private String pguIpshUrl;

    @Value("${mock.billing.enabled}")
    private Boolean mockEnabled;
    @Value("${mock.billing.url:#{null}}")
    private String mockUrl;

    private final RestTemplate restTemplate;
    private final DictionaryResponseMapper mapper;

    @Autowired
    private NsiDictionaryService nsiDictionaryService;

    @Override
    public BillInfoResponseWrapper getBillInfo(String token, String billId) {
        return billTemplateRequest(token, BILL_INFO_URL, HttpMethod.POST, BillInfoResponseWrapper.class,
                Collections.emptyMap(), Map.of(
                        BILL_IDS_PARAM, billId,
                        CI_PARAM, false));
    }

    @Override
    public BillInfoResponseWrapper getBillInfoByBillNumber(String token, String billNumber) {
        return billTemplateRequest(token, BILL_INFO_URL_BY_BILL_NUMBER, HttpMethod.POST, BillInfoResponseWrapper.class,
                Collections.emptyMap(), Map.of(
                        "billNumber", billNumber,
                        CI_PARAM, false));
    }

    @Override
    public ImportBillStatusResponse getBillStatus(String token, String requestId) {
        return billTemplateRequest(token, IMPORT_BILL_STATUS_URL, HttpMethod.GET, ImportBillStatusResponse.class,
                Collections.emptyMap(), Map.of("requestId", requestId));
    }

    @Override
    public ImportBillNewResponse getNewBillNumber(String token, Map<String, Object> parameters) {
        return billTemplateRequest(token, IMPORT_BILL_NEW_URL, HttpMethod.POST, ImportBillNewResponse.class,
                parameters, Collections.emptyMap());
    }

    public <T> T billTemplateRequest(String token, String path, HttpMethod httpMethod, Class<T> classType,
                                     Map<String, Object> body, Map<String, Object> pathVariables) {
        HttpEntity<Map<String, Object>> entity = getHttpEntityWithBody(token, body);
        String url = String.format("%s%s", getIpshServiceUrl(), path);
        try {
            ResponseEntity<T> response = restTemplate.exchange(url, httpMethod, entity, classType, pathVariables);
            return response.getBody();
        } catch (RestClientException e) {
            throw new ExternalServiceException(e);
        }
    }

    @Override
    public String getBillPdfURI(String billId) {
        return UriComponentsBuilder.fromUriString(String.format("%s%s", getIpshServiceUrl(), BILL_PDF_URL_EXT))
                .queryParam(BILL_IDS_PARAM, billId).build(false).toString();
    }

    private <T> HttpEntity<T> getHttpEntityWithBody(String token, T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "acc_t=" + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    /**
     * Формирование billId
     */
    @Override
    public PaymentPossibilityResponse getPaymentPossibleDecision(final PaymentPossibilityRequest request) {
        PaymentPossibilityResponse result = PaymentPossibilityResponse.builder()
                .applicantType(request.getApplicantType())
                .build();
        try {
            setOrganizationRequisites(request, result);
            setPaymentRequisites(request, result);
            setPayAmounts(request, result);
            createBill(request, result);
        } catch (PguException | RestClientException ex) {
            log.error(ex.getMessage());
            result.setState(PaymentPossibilityResponse.PaymentPossibilityRequestState.SERVICE_ERROR);
            result.setErrorMessage("Ошибка при формировании счета: " + ex.getMessage());
        }
        return result;
    }

    @Override
    public boolean isBillPaid(BillInfoResponseWrapper billInfoResponse) {
        List<BillInfo> bills = billInfoResponse.getResponse().getBills();
        if (CollectionUtils.isEmpty(bills)) {
            return false;
        }
        BillInfo billInfo = bills.get(0);
        Integer errorCode = billInfoResponse.getError().getCode();
        return billInfo.getIsPaid() || errorCode == 22 || errorCode == 23;
    }

    @Override
    public BillData setBillToOrder(String token, String serviceId, Long orderId, String billNumber) {

        HttpEntity<List<NameValueContainer>> entity = getHttpEntityWithBody(token, Collections.emptyList());
        String url = String.format("%s%s", pguUrl, SET_BILL_TO_ORDER_URL);
        Map<String, String> uriParams = Map.of(
                "orderId", String.valueOf(orderId),
                "billNumber", billNumber
        );
        ParameterizedTypeReference<BillData> typeReference = new ParameterizedTypeReference<>(){};
        return restTemplate.exchange(url, HttpMethod.POST, entity, typeReference, uriParams).getBody();
    }



    public void setOrganizationRequisites(final PaymentPossibilityRequest request, final PaymentPossibilityResponse result) {
        if(Objects.nonNull(request.getRetryOrgRequisites()) && !request.getRetryOrgRequisites().isEmpty()) {
            OrganizationRequisites organizationRequisites = mapper.mapOrganizationRequisites(request.getRetryOrgRequisites());
            result.setOrganizationRequisites(organizationRequisites);
            return;
        }

        NsiDictionaryFilterRequest nsiRequest = getNsiDictionaryFilterRequest(
                request.getOrgRequisitesDictionaryTx(),
                getNsiDictionaryFilterList(request.getOrgRequisitesFilters()));


        NsiDictionary response = nsiDictionaryService.getDictionary(request.getOrgRequisitesDictionary(), nsiRequest);
        response.getItems().stream().findAny()
                .map(NsiDictionaryItem::getAttributeValues)
                .map(el -> {
                    request.setRetryOrgRequisites(el);
                    if (request.getOrgRequisitesResponseVersion().equals("2")) {
                        return mapper.mapOrganizationRequisitesV2(el);
                    }
                    return mapper.mapOrganizationRequisites(el);
                })
                .ifPresent(result::setOrganizationRequisites);
    }

    public void setPaymentRequisites(final PaymentPossibilityRequest request, final PaymentPossibilityResponse result) {
        if(Objects.nonNull(request.getRetryPayRequisites()) && !request.getRetryPayRequisites().isEmpty()) {
            PaymentRequisites paymentRequisites = mapper.mapPaymentRequisites(request.getRetryPayRequisites());
            result.setPaymentRequisites(paymentRequisites);
            return;
        }

        NsiDictionaryFilterRequest nsiRequest = getNsiDictionaryFilterRequest(
                request.getPayRequisitesDictionaryTx(),
                getNsiDictionaryFilterList(request.getPayRequisitesFilters())
        );
        NsiDictionary response = nsiDictionaryService.getDictionary(REQUISITE_STATE_DUTY, nsiRequest);

        response.getItems().stream().findAny()
                .map(NsiDictionaryItem::getAttributeValues)
                .map(el -> {
                    request.setRetryPayRequisites(el);
                    return mapper.mapPaymentRequisites(el);
                })
                .ifPresent(result::setPaymentRequisites);
    }

    public void setPayAmounts(final PaymentPossibilityRequest request, final PaymentPossibilityResponse result) {
        if(Objects.nonNull(request.getRetryPayFullAmount()) && request.getRetryPayFullAmount() > 0) {
            result.setRequestFullAmount(String.valueOf(request.getRetryPayFullAmount()));
            result.setRequestSaleAmount(String.valueOf(request.getRetryPaySaleAmount()));
            return;
        }
        NsiSimpleDictionaryFilterContainer simpleService = createSimple("SERVICE", "EQUALS", request.getServiceCode());
        NsiSimpleDictionaryFilterContainer simpleApplicant = createSimple("APPLICANTTYPE", "EQUALS", request.getApplicantType());

        List<NsiDictionaryFilter> amountCodesSimpleFilters = new ArrayList<>();

        request.getAmountCodes().forEach(amountCode ->
            amountCodesSimpleFilters.add(createSimple("AMOUNTCODE", "EQUALS", amountCode)));

        NsiUnionDictionaryFilterContainer unionFilter = createUnion(amountCodesSimpleFilters, NsiDictionaryUnionType.OR);

        NsiDictionaryFilterRequest nsiRequest = getNsiDictionaryFilterRequest("", List.of(simpleService, simpleApplicant, unionFilter));
        NsiDictionary nsiResponse = nsiDictionaryService.getDictionary(STATE_DUTY, nsiRequest);

        nsiResponse.getItems().stream()
                .map(NsiDictionaryItem::getAttributeValues)
                .forEach(attrs -> {
                    String fullamount = attrs.get("FULLAMOUNT");
                    request.addFullAmountCodePrice(attrs.get("AMOUNTCODE"), Integer.parseInt(fullamount));
                    String saleAmount = attrs.get("SALEAMOUNT");
                    request.addSaleAmountCodePrice(attrs.get("AMOUNTCODE"),  Integer.parseInt(Optional.ofNullable(saleAmount).orElse(fullamount)));
                });

        Integer fullBillSum = request.getFullAmountCodePrices().values().stream().mapToInt(Integer::intValue).sum();
        fullBillSum *= CURRENCY_MULTIPLY_UNIT;
        request.setRetryPayFullAmount(fullBillSum);
        result.setRequestFullAmount(String.valueOf(fullBillSum));

        Integer saleBillSum = request.getSaleAmountCodePrices().values().stream().mapToInt(Integer::intValue).sum();
        request.setRetryPaySaleAmount(saleBillSum);
        result.setRequestSaleAmount(String.valueOf(saleBillSum));
    }

    public void createBill(PaymentPossibilityRequest request, final PaymentPossibilityResponse result) {
        if(StringUtils.isEmpty(result.getPaymentRequisites()) || ZERO_AMOUNT_VALUE.equals(result.getRequestFullAmount())) {
            log.error("Empty or zero Sum error for bill create request. orderId : {}, amount_codes: {}", request.getOrderId(), request.getAmountCodes());
            result.setState(PaymentPossibilityResponse.PaymentPossibilityRequestState.REQUSITE_ERROR);
            result.setErrorMessage("No payment requisites or amount is 0. Requisites: " + result.getPaymentRequisites() + ", amount: " + result.getRequestFullAmount());
            return;
        }
        if(Objects.isNull(result.getPaymentRequisites()) || Objects.isNull(result.getOrganizationRequisites())) {
                log.error("Empty [OrganizationRequisites, PaymentRequisites] error for bill create request. orderId : {}, organization_code: {}", request.getOrderId(), request.getOrganizationId());
                result.setState(PaymentPossibilityResponse.PaymentPossibilityRequestState.REQUSITE_ERROR);
                result.setErrorMessage("Empty [OrganizationRequisites, PaymentRequisites] error for bill create request. orderId: " + request.getOrderId() + ", organization_code: " + request.getOrganizationId());
                return;
        }
        List<NameValueContainer> billParams = new ArrayList<>();
        billParams.add(new NameValueContainer("epgu_order_id", String.valueOf(request.getOrderId())));
        billParams.addAll(result.getOrganizationRequisites().getParams());
        billParams.addAll(result.getPaymentRequisites().getParams());
        billParams.add(new NameValueContainer("Sum", result.getRequestFullAmount()));
        billParams.add(new NameValueContainer("returnUrl", request.getReturnUrl()));
        billParams.add(new NameValueContainer("returnUrlOrder", request.getReturnUrl()));
        if (StringUtils.hasText(request.getPayerIdType()) && StringUtils.hasText(request.getPayerIdNum())) {
            billParams.add(new NameValueContainer("payerIdType", request.getPayerIdType()));
            billParams.add(new NameValueContainer("payerIdNum", request.getPayerIdNum()));
        }

        HttpEntity<List<NameValueContainer>> entity = getHttpEntityWithBody(request.getToken(), billParams);
        String personalType = "UL".equals(request.getApplicantType()) ? "O" : "P";
        String url = String.format(PAYGATE_BETA_REQ_API, getServiceUrl());

        try {
            LinkedHashMap<String, String> uriParams = new LinkedHashMap<>(4);
            uriParams.put("orderId", String.valueOf(request.getOrderId()));
            uriParams.put("type", personalType);
            ParameterizedTypeReference<Map<String, Object>> typeReference = new ParameterizedTypeReference<>(){};
            Map<String, Object> response = restTemplate.exchange(url, HttpMethod.POST, entity, typeReference, uriParams).getBody();
            if (Objects.isNull(response) || response.isEmpty() || !response.containsKey("content")) {
                log.error("requsite error for bill create request with params: {}", billParams);
                result.setState(PaymentPossibilityResponse.PaymentPossibilityRequestState.REQUSITE_ERROR);
                result.setErrorMessage("Requsite error for bill create request. OrderId: " + request.getOrderId() + ", type: " + personalType);
                return;
            }
            Map<String, String> content = (Map<String, String>) response.get("content");
            String billNumber = content.get("billNumber");
            String billId = content.get("billId");

            request.setBillNumber(billNumber);
            request.setBillId(billId);

            result.setBillId(billId);
            result.setBillNumber(billNumber);

            result.setState(PaymentPossibilityResponse.PaymentPossibilityRequestState.SUCCESS);
        } catch (EntityNotFoundException e) {
            log.error("Error create bill. external paygate service status not found. url: {}, orderId: {}, personType: {}, params: {}", url, request.getOrderId(), personalType, billParams);
            result.setBillId("");
            result.setBillNumber("");
            throw new EntityNotFoundException("Error create bill. external paygate service status not found. url: " + url + ", orderId: " + request.getOrderId() + ", personType: " + personalType);
        } catch (RestClientException e) {
            throw new ExternalServiceException(e);
        }
    }

    private List<NsiDictionaryFilter> getNsiDictionaryFilterList(List<Map<String, String>> simpleList) {
        List<NsiDictionaryFilter> subs = new ArrayList<>();

        for (Map<String, String> simpleMap : simpleList) {
            String attributeName = simpleMap.get("attributeName");
            String condition = simpleMap.get("condition");
            String value = simpleMap.get("value");
            NsiSimpleDictionaryFilterContainer simpleContainer = createSimple(attributeName, condition, value);
            subs.add(simpleContainer);
        }

        return subs;
    }

    private NsiDictionaryFilterRequest getNsiDictionaryFilterRequest(String tx, List<NsiDictionaryFilter> subs) {

        NsiDictionaryFilterUnion union = new NsiDictionaryFilterUnion();
        union.setUnionKind(NsiDictionaryUnionType.AND);
        union.setSubs(subs);

        NsiUnionDictionaryFilterContainer dictionaryFilter = new NsiUnionDictionaryFilterContainer();
        dictionaryFilter.setUnion(union);

        NsiDictionaryFilterRequest.Builder requestBuilder = new NsiDictionaryFilterRequest.Builder();
        requestBuilder
                .setTreeFiltering("ONELEVEL")
                .setPageNum("1")
                .setPageSize("258")
                .setSelectAttributes(List.of("*"))
                .setFilter(dictionaryFilter);
        NsiDictionaryFilterRequest nsiRequest = requestBuilder.build();
        nsiRequest.setTx(tx);
        nsiRequest.setParentRefItemValue("");
        return nsiRequest;
    }


    private NsiSimpleDictionaryFilterContainer createSimple(String attributeName, String condition, String value) {
        NsiSimpleDictionaryFilterContainer simpleContainer = new NsiSimpleDictionaryFilterContainer();
        NsiDictionaryFilterSimple simple = new NsiDictionaryFilterSimple();
        NsiDictionaryFilterSimpleValue simpleValue = new NsiDictionaryFilterSimpleValue();
        simpleValue.putAttributeValue("asString", value);
        simple.setAttributeName(attributeName);
        simple.setCondition(condition);
        simple.setValue(simpleValue);
        simpleContainer.setSimple(simple);
        return simpleContainer;
    }

    private NsiUnionDictionaryFilterContainer createUnion(List<NsiDictionaryFilter> subs, NsiDictionaryUnionType unionKind) {
        NsiUnionDictionaryFilterContainer unionContainer = new NsiUnionDictionaryFilterContainer();
        NsiDictionaryFilterUnion union = new NsiDictionaryFilterUnion();
        union.setSubs(subs);
        union.setUnionKind(unionKind);
        unionContainer.setUnion(union);
        return unionContainer;
    }

    private Map<String, String> createFilterMap(String attributeName, String condition, String value) {
        return Map.of(
                "attributeName", attributeName,
                "condition", condition,
                "value", value);
    }

    private String getServiceUrl() {
        return mockEnabled ? mockUrl : pguUrl;
    }

    private String getIpshServiceUrl() {
        return mockEnabled ? mockUrl : pguIpshUrl;
    }
}
