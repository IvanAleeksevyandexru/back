package ru.gosuslugi.pgu.fs.component.payment;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import ru.atc.carcass.security.rest.model.person.PersonDoc;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.common.core.exception.ValidationException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry;
import ru.gosuslugi.pgu.fs.common.variable.VariableType;
import ru.gosuslugi.pgu.fs.component.payment.model.CommonDataBox;
import ru.gosuslugi.pgu.fs.component.payment.model.PayerInfo;
import ru.gosuslugi.pgu.fs.component.payment.strategy.BillContainerService;
import ru.gosuslugi.pgu.fs.component.payment.strategy.PaymentSelectorProcess;
import ru.gosuslugi.pgu.pgu_common.payment.dto.PaymentInfo;
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.BillInfo;
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.BillInfoResponseWrapper;
import ru.gosuslugi.pgu.pgu_common.payment.dto.pay.PaymentPossibilityRequest;
import ru.gosuslugi.pgu.pgu_common.payment.dto.pay.PaymentPossibilityResponse;
import ru.gosuslugi.pgu.pgu_common.payment.dto.pay.PaymentPossibilityResponse.PaymentPossibilityRequestState;
import ru.gosuslugi.pgu.pgu_common.payment.service.BillingService;
import ru.gosuslugi.pgu.pgu_common.payment.service.PaymentService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.gosuslugi.pgu.components.ComponentAttributes.BILL_ID_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.BILL_NUMBER_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.FULL_AMOUNT_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.IGNORE_ORGCODE;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ORGANIZATION_ID_ARG_KEY;
import static ru.gosuslugi.pgu.components.ComponentAttributes.SALE_AMOUNT_ATTR;
import static ru.gosuslugi.pgu.components.FieldComponentUtil.ACTIONS_ATTR_KEY;
import static ru.gosuslugi.pgu.pgu_common.payment.dto.pay.PaymentPossibilityResponse.PaymentPossibilityRequestState.BILL_PAID;
import static ru.gosuslugi.pgu.pgu_common.payment.dto.pay.PaymentPossibilityResponse.PaymentPossibilityRequestState.REQUSITE_ERROR;
import static ru.gosuslugi.pgu.pgu_common.payment.dto.pay.PaymentPossibilityResponse.PaymentPossibilityRequestState.SERVICE_ERROR;
import static ru.gosuslugi.pgu.pgu_common.payment.dto.pay.PaymentPossibilityResponse.PaymentPossibilityRequestState.SUCCESS;

/**
 * Компонент по поддержке способа оплаты госпошлины
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentTypeSelectorComponent extends AbstractComponent<String> {

    public static final String ORG_REQUISITES_DICTIONARY = "orgRequisitesDictionary";
    public static final String ORG_REQUISITES_DICTIONARY_TX = "orgRequisitesDictionaryTx";
    public static final String ORG_REQUISITES_FILTERS = "orgRequisitesFilters";
    public static final String PAY_REQUISITES_DICTIONARY_TX = "payRequisitesDictionaryTx";
    public static final String PAY_REQUISITES_FILTERS = "payRequisitesFilters";
    public static final String ACTION_OPERATION_KEY = "operation";
    public static final String RETRY_BILL_CREATE = "retry";
    public static final String RETRY_ORG_REQUISITE = "retryOrgRequisite";
    public static final String RETRY_PAY_REQUISITE = "retryPayRequisite";
    public static final String RETRY_FULL_AMOUNT = "retryAmount";
    public static final String RETRY_SALE_AMOUNT = "retrySaleAmount";

    private static final String AMOUNT_CODE_ARG_KEY = "amountCode";
    private static final String APPLICANT_TYPE_ARG_KEY = "applicantType";
    private static final String ARGS_HASH_KEY = "hash";
    private static final String REQUEST_STATE_KEY = "state";
    private static final String STATES_KEY = "states";
    private static final String SHOW_ONLY_FOR_UNUSED_PAYMENTS_KEY = "showOnlyForUnusedPayments";
    private static final String ERROR_MESSAGE_ARG_KEY = "errorMessage";

    private final static String VERIFIED_ATTR_VAL = "VERIFIED";
    private static final String FL_APPLICANT_TYPE = "FL";
    private static final String UL_APPLICANT_TYPE = "UL";
    private static final String IP_APPLICANT_TYPE = "IP";
    private static final String FL_SPECIFIC_PAYERID = "specificPayerIdTypeByOwnerDocumentTypeCode";
    private static final String APPLICANT_STRING_VALUE = "applicantTypeStringValue";
    public static final String ORG_REQUISITES_RESPONSE_VERSION = "orgRequisitesResponseVersion";

    private final PaymentService paymentService;
    private final BillingService billingService;
    private final UserPersonalData userPersonalData;
    private final UserOrgData userOrgData;
    private final JsonProcessingService jsonProcessingService;
    private final VariableRegistry variableRegistry;
    private final BillContainerService billContainerStrategy;

    public static final String DEFAULT_PAYER_ID_TYPE_FL = "01";
    public static final String DEFAULT_PAYER_ID_TYPE_UL = "99";
    public static final String DEFAULT_PAYER_ID_TYPE_IP = "98";
    public static final String DEFAULT_PAYER_ID_TYPE_FOREIGN_CITIZEN = "08";
    public static final String FL_SPECIFIC_PAYERID_VALUE = "08";

    @Override
    public ComponentType getType() {
        return ComponentType.PaymentTypeSelector;
    }

    @Override
    public void preProcess(FieldComponent component, ScenarioDto scenarioDto) {
        PaymentSelectorProcess paySelectProcess = new PaymentSelectorProcess(billContainerStrategy, this);
        paySelectProcess.of(component, scenarioDto)
                .completeIf(paySelectProcess::hasDataInDisplay, paySelectProcess::setAttrsFromDisplay)
                .completeIf(paySelectProcess::isSuccessUseBillContainer)
                .execute(paySelectProcess::componentDefaultInit)
                .start();
    }

    public CommonDataBox<PaymentPossibilityRequest, PaymentPossibilityResponse> defaultInit(FieldComponent component, ScenarioDto scenarioDto){
        PaymentPossibilityRequest request = preparePossibilityRequest(component, scenarioDto);
        PaymentPossibilityResponse result = preparePossibilityResponse(component, scenarioDto, request);
        if (result == null) {
            result = billingService.getPaymentPossibleDecision(request);
            saveRequsitesInComponentAttrs(component, request);
        }
        String serviceId = variableRegistry.getVariable(VariableType.serviceId).getValue(scenarioDto);

        processResult(component, request, result, serviceId);

        return new CommonDataBox(request, result)
                .withComponent(component)
                .withScenario(scenarioDto)
                .withServiceId(serviceId);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        ApplicantAnswer answer = entry.getValue();
        Map<String, String> valueMap = (Map<String, String>) AnswerUtil.tryParseToMap(answer.getValue());
        if (RETRY_BILL_CREATE.equals(valueMap.get(ACTION_OPERATION_KEY))) {
            retryCreateBill(scenarioDto, entry.getKey(), fieldComponent);
            incorrectAnswers.put(entry.getKey(), "повтор создания начисления");
        }
    }

    private void retryCreateBill(ScenarioDto scenarioDto, String key, FieldComponent fieldComponent) {
        Optional<FieldComponent> fieldBox = scenarioDto.getDisplay().getComponents().stream().filter(el -> el.getId().equals(key)).findAny();
        if (fieldBox.isPresent()) {
            FieldComponent field = fieldBox.get();
            field.setAttrs(fieldComponent.getAttrs());
            PaymentPossibilityRequest request = preparePossibilityRequest(field, scenarioDto);
            PaymentPossibilityResponse result = billingService.getPaymentPossibleDecision(request);
            saveRequsitesInComponentAttrs(field, request);
            String serviceId = variableRegistry.getVariable(VariableType.serviceId).getValue(scenarioDto);
            processResult(field, request, result, serviceId);
            if (SUCCESS == result.getState()) {
                CommonDataBox<PaymentPossibilityRequest, PaymentPossibilityResponse> box = new CommonDataBox(request, result)
                        .withComponent(fieldComponent)
                        .withScenario(scenarioDto)
                        .withServiceId(serviceId);
                billContainerStrategy.refreshBillContainer(box);
            }
        }
    }

    private PaymentPossibilityResponse getPreviousPaymentDecision(FieldComponent field, PaymentPossibilityRequest request, Map<String, String> valueMap) {
        return PaymentPossibilityResponse.builder()
                .applicantType(request.getApplicantType())
                .state(getPreviousPaymentState(field, valueMap))
                .billId(valueMap.get(BILL_ID_ATTR))
                .billNumber(valueMap.get(BILL_NUMBER_ATTR))
                .requestFullAmount(valueMap.get(FULL_AMOUNT_ATTR))
                .requestSaleAmount(valueMap.get(SALE_AMOUNT_ATTR))
                .build();
    }

    private PaymentPossibilityRequestState getPreviousPaymentState(FieldComponent field, Map<String, String> valueMap) {
        PaymentPossibilityRequestState state = PaymentPossibilityRequestState.valueOf((String) field.getAttrs().get(REQUEST_STATE_KEY));
        String billId = valueMap.get(BILL_ID_ATTR);
        BillInfoResponseWrapper billInfoResponseWrapper = billingService.getBillInfo(userPersonalData.getToken(), billId);
        Integer errorCode = billInfoResponseWrapper.getError().getCode();
        List<BillInfo> bills = billInfoResponseWrapper.getResponse().getBills();
        if (CollectionUtils.isEmpty(bills)) {
            return SERVICE_ERROR;
        }
        BillInfo billInfo = bills.get(0);
        if (billInfo.getIsPaid() || errorCode == 22 || errorCode == 23) {
            return BILL_PAID;
        }
        return state;
    }

    @SuppressWarnings("unchecked")
    public void processResult(FieldComponent field, PaymentPossibilityRequest request, PaymentPossibilityResponse result, String serviceId) {
        if (Objects.isNull(result)) {
            result = PaymentPossibilityResponse.builder()
                    .state(REQUSITE_ERROR)
                    .billNumber("")
                    .billId("")
                    .build();
        }
        field.getAttrs().put(REQUEST_STATE_KEY, result.getState().name());
        field.getAttrs().put(APPLICANT_TYPE_ARG_KEY, request.getApplicantType());
        if (StringUtils.hasText(result.getErrorMessage())) {
            field.getAttrs().put(ERROR_MESSAGE_ARG_KEY, result.getErrorMessage());
        }

        final String billId = result.getBillId();
        final String billNumber = result.getBillNumber();
        final String fullAmount = result.getRequestFullAmount();
        final String saleAmount = result.getRequestSaleAmount();

        // проверяем есть ли оплаченные и не использованные ранее пошлины
        boolean alreadyPaid = hasUnusedPayments(field, request.getOrderId(), fullAmount, saleAmount, serviceId);

        Map<String, Map<String, Object>> states = (Map<String, Map<String, Object>>) field.getAttrs().getOrDefault(STATES_KEY, Collections.emptyMap());
        states.values().forEach(state -> {
            List<Map<String, String>> actions = (List<Map<String, String>>) state.get(ACTIONS_ATTR_KEY);
            if (!alreadyPaid) {
                actions = actions.stream()
                        .filter(it -> !it.containsKey(SHOW_ONLY_FOR_UNUSED_PAYMENTS_KEY))
                        .collect(Collectors.toList());
            }
            actions.forEach(action -> {
                Map<String, String> value = new HashMap<>();
                value.put(BILL_ID_ATTR, billId);
                value.put(BILL_NUMBER_ATTR, billNumber);
                value.put(FULL_AMOUNT_ATTR, fullAmount);
                value.put(SALE_AMOUNT_ATTR, saleAmount);
                value.put("selected", action.get("label"));
                value.put(ARGS_HASH_KEY, String.valueOf(request.hashCode()));
                if (action.containsKey(ACTION_OPERATION_KEY)) {
                    value.put(ACTION_OPERATION_KEY, RETRY_BILL_CREATE);
                }
                action.put("value", JsonProcessingUtil.toJson(value));
            });
            state.put(ACTIONS_ATTR_KEY, actions);
        });
    }

    private void saveRequsitesInComponentAttrs(FieldComponent field, PaymentPossibilityRequest request) {
        Map<String, Object> fieldAttrs = field.getAttrs();
        fieldAttrs.put(RETRY_ORG_REQUISITE, request.getRetryOrgRequisites());
        fieldAttrs.put(RETRY_PAY_REQUISITE, request.getRetryPayRequisites());
        fieldAttrs.put(RETRY_FULL_AMOUNT, request.getRetryPayFullAmount());
        fieldAttrs.put(RETRY_SALE_AMOUNT, request.getRetryPaySaleAmount());
    }

    @SuppressWarnings("unchecked")
    private PaymentPossibilityRequest preparePossibilityRequest(FieldComponent component, ScenarioDto scenarioDto) {
        Map<String, String> arguments = component.getArguments();
        List<String> amountCodes = IntStream.rangeClosed(1, 20)
                .mapToObj(el -> arguments.get(AMOUNT_CODE_ARG_KEY+el))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        String applicantType = component.getArgument(APPLICANT_TYPE_ARG_KEY);
        String organizationId = component.getArgument(ORGANIZATION_ID_ARG_KEY);
        Map<String, Object> attrs = component.getAttrs();
        PayerInfo payerInfo = getPayerIdTypeAndPayerIdNum(component, applicantType, scenarioDto.getCurrentValue(), scenarioDto.getApplicantAnswers());

        Map<String, Object> displayAttrs = Collections.emptyMap();
        Optional<FieldComponent> displayComponent = scenarioDto.getDisplay().getComponents().stream().filter(el -> el.getId().equals(component.getId())).findAny();
        if (displayComponent.isPresent()) {
            displayAttrs = displayComponent.get().getAttrs();
        }

        String orgRequisitesResponseVersion = "1";
        if (component.getAttrs().containsKey(ORG_REQUISITES_RESPONSE_VERSION)) {
            orgRequisitesResponseVersion = String.valueOf(component.getAttrs().get(ORG_REQUISITES_RESPONSE_VERSION));
        }
        String serviceId = variableRegistry.getVariable(VariableType.serviceId).getValue(scenarioDto);
        PaymentPossibilityRequest request = PaymentPossibilityRequest.builder()
                .amountCodes(amountCodes)
                .applicantType(applicantType.isEmpty() ? "FL" : applicantType)
                .organizationId(organizationId)
                .orderId(scenarioDto.getOrderId())
                .serviceId(Optional.ofNullable(serviceId).orElseGet(scenarioDto::getServiceCode)) // в некоторых услугах (например в апостиле) нет serviceIds блока
                .serviceCode(scenarioDto.getServiceCode())
                .fullAmountCodePrices(new HashMap<>())
                .saleAmountCodePrices(new HashMap<>())
                .token(userPersonalData.getToken())
                .orgRequisitesDictionary(String.valueOf(attrs.get(ORG_REQUISITES_DICTIONARY)))
                .orgRequisitesDictionaryTx(String.valueOf(attrs.get(ORG_REQUISITES_DICTIONARY_TX)))
                .orgRequisitesFilters((List<Map<String, String>>) attrs.get(ORG_REQUISITES_FILTERS))
                .payRequisitesDictionaryTx(String.valueOf(attrs.get(PAY_REQUISITES_DICTIONARY_TX)))
                .payRequisitesFilters((List<Map<String, String>>) attrs.get(PAY_REQUISITES_FILTERS))
                .returnUrl(scenarioDto.getCurrentUrl() + "?getLastScreen=1")
                .payerIdType(payerInfo.getIdType())
                .payerIdNum(payerInfo.getIdNum())
                //params for reuse
                .retryOrgRequisites((Map<String, String>) displayAttrs.get(RETRY_ORG_REQUISITE))
                .retryPayRequisites((Map<String, String>) displayAttrs.get(RETRY_PAY_REQUISITE))
                .retryPayFullAmount((Integer) displayAttrs.get(RETRY_FULL_AMOUNT))
                .retryPaySaleAmount((Integer) displayAttrs.get(RETRY_SALE_AMOUNT))
                .orgRequisitesResponseVersion(orgRequisitesResponseVersion)
                .build();
        request.setDictionaryFilterValues(request.getOrgRequisitesFilters());
        request.setDictionaryFilterValues(request.getPayRequisitesFilters());
        return request;
    }

    public PayerInfo getPayerIdTypeAndPayerIdNum(FieldComponent component, String applicantType, Map<String, ApplicantAnswer> currentAnswer, Map<String, ApplicantAnswer> previousAnswers) {
        String payerIdType;
        String payerIdNum;
        applicantType = applicantType.isEmpty() ? "FL" : applicantType;
        boolean applicantTypeFL = FL_APPLICANT_TYPE.equalsIgnoreCase(applicantType);
        boolean applicantTypeUL = UL_APPLICANT_TYPE.equalsIgnoreCase(applicantType);
        boolean applicantTypeIP = IP_APPLICANT_TYPE.equalsIgnoreCase(applicantType);

        if (!"RUS".equalsIgnoreCase(userPersonalData.getPerson().getCitizenshipCode())) {
            PersonDoc foreignPassport =
                    userPersonalData.getDocs().stream()
                            .filter(x -> ("FID_DOC".equals(x.getType()) && x.getVrfStu().equals(VERIFIED_ATTR_VAL)))
                            .findFirst()
                            .orElseThrow(() -> new ValidationException("Couldn't find foreign passport"));
            payerIdType = DEFAULT_PAYER_ID_TYPE_FOREIGN_CITIZEN;
            payerIdNum = foreignPassport.getSeries() + foreignPassport.getNumber();
            return new PayerInfo(payerIdType, payerIdNum);
        }
        String applicantTypeStringValue = component.getArgument(APPLICANT_STRING_VALUE);
        DocumentContext currentDocumentContext = JsonPath.parse(currentAnswer);
        DocumentContext previousDocumentContext = JsonPath.parse(previousAnswers);
        if (applicantTypeFL) {
            PersonDoc rfPassport = userPersonalData.getDocs().stream()
                    .filter(x -> ("RF_PASSPORT".equals(x.getType()) && x.getVrfStu().equals(VERIFIED_ATTR_VAL)))
                    .findFirst()
                    .orElseThrow(() -> new ValidationException("Couldn't find russian passport"));
            payerIdType = DEFAULT_PAYER_ID_TYPE_FL;
            payerIdNum = rfPassport.getSeries() + rfPassport.getNumber();
            String specificPayerId = component.getArgument(FL_SPECIFIC_PAYERID);

            PayerInfo payerInfo = getPayerInfoOrDefault(applicantTypeStringValue, currentDocumentContext,
                    previousDocumentContext, new PayerInfo(payerIdType, payerIdNum));
            if (StringUtils.hasText(specificPayerId)) {
                payerInfo.setIdType(specificPayerId);
            }
            return payerInfo;
        }

        if (applicantTypeUL || applicantTypeIP) {
            // сначало пытаемся вытянуть значения из аргумента "applicantTypeStringValue"
            PayerInfo payerInfo = getPayerInfo(applicantTypeStringValue, currentDocumentContext, previousDocumentContext);
            if (payerInfo.getIdNum() != null) {
                return payerInfo;
            }

            if (applicantTypeUL) {
                if (userOrgData == null || userOrgData.getOrg() == null) {
                    throw new FormBaseException("Учетная запись пользователь не является организацией");
                }
                return new PayerInfo(DEFAULT_PAYER_ID_TYPE_UL, userOrgData.getOrg().getInn() + userOrgData.getOrg().getKpp());
            }
            return new PayerInfo(DEFAULT_PAYER_ID_TYPE_IP, userPersonalData.getPerson().getInn());
        }

        return new PayerInfo();
    }

    private PayerInfo getPayerInfoOrDefault(String applicantTypeStringValue, DocumentContext current, DocumentContext previous,
                                            PayerInfo defaultPayerInfo) {
        PayerInfo payerInfo = getPayerInfo(applicantTypeStringValue, current, previous);
        if (payerInfo.getIdNum() == null) {
            return defaultPayerInfo;
        }
        return payerInfo;
    }

    private PayerInfo getPayerInfo(String applicantTypeStringValue, DocumentContext current, DocumentContext previous) {
        PayerInfo result = new PayerInfo();
        if (StringUtils.hasText(applicantTypeStringValue)) {
            String[] payerInfo = applicantTypeStringValue.split(",");
            if (payerInfo.length != 2) {
                return result;
            }
            result.setIdType(payerInfo[0]);
            String[] docNumAndSeries = payerInfo[1].split("[+]");
            result.setIdNum(getPayerIdNum(docNumAndSeries, current, previous));
        }

        return result;
    }

    private String getPayerIdNum(String[] values, DocumentContext current, DocumentContext previous) {
        if (values.length == 1) {
            return getValueFromAnswers(values[0], current, previous);
        }
        if (values.length == 2) {
            return getValueFromAnswers(values[0], current, previous) + getValueFromAnswers(values[1], current, previous);
        }
        return "";
    }

    private String getValueFromAnswers(String field, DocumentContext currentAnswer, DocumentContext previousAnswers) {
        String value = Optional.ofNullable(jsonProcessingService.getFieldFromContext(field, currentAnswer, ApplicantAnswer.class)).map(ApplicantAnswer::getValue).orElse("");
        if (StringUtils.hasText(value)) {
            return value;
        }
        return Optional.ofNullable(jsonProcessingService.getFieldFromContext(field, previousAnswers, ApplicantAnswer.class)).map(ApplicantAnswer::getValue).orElse("");
    }

    @SuppressWarnings("unchecked")
    private PaymentPossibilityResponse preparePossibilityResponse(FieldComponent component, ScenarioDto scenarioDto, PaymentPossibilityRequest request) {
        String componentId = component.getId();
        ApplicantAnswer applicantAnswer = Optional.ofNullable(scenarioDto.getCachedAnswers().get(componentId)).orElse(scenarioDto.getApplicantAnswers().get(componentId));
        if (Objects.isNull(applicantAnswer) || StringUtils.isEmpty(applicantAnswer.getValue())) {
            return null;
        }
        Map<String, String> valueMap = (Map<String, String>) AnswerUtil.tryParseToMap(applicantAnswer.getValue());
        String hash = valueMap.get(ARGS_HASH_KEY);
        return  request.hashCode() == Integer.parseInt(hash) && StringUtils.hasText(valueMap.get(BILL_ID_ATTR)) ?
                getPreviousPaymentDecision(component, request, valueMap) : null;
    }

    /**
     * Проверяет имеются ли неиспользованные ранее оплаченные пошлины.
     * Дополнительно после запроса осуществляется сравнение сумм из ответа с суммами, которые долны быть (для юр.лиц полная стоимость, для остальных скидочная)
     * @param field компонент
     * @param orderId id заявления
     * @param fullAmount полная стоимость (всегда передаётся в body
     * @param saleAmount стоимость со скидкой
     * @param serviceId id сервиса
     * @return наличие или отсутствие неиспользованных платежей
     */
    private Boolean hasUnusedPayments(FieldComponent field, Long orderId, String fullAmount, String saleAmount, String serviceId) {
        String value = field.getArgument(IGNORE_ORGCODE);
        boolean ignoreOrgCode = Boolean.parseBoolean(value);
        String orgCode = ignoreOrgCode ? null : field.getArgument(ORGANIZATION_ID_ARG_KEY);
        String applicantType = field.getArgument(APPLICANT_TYPE_ARG_KEY);
        applicantType = applicantType.isEmpty() ? "FL" : applicantType;
        // так как это доп. функционал - ошибки в вызове сервиса ранее оплаченных пошлин не должны влиять на работу компонента
        try {
            List<PaymentInfo> unusedPayments = paymentService.getUnusedPaymentsV3(orderId, orgCode, userPersonalData.getToken(), serviceId, applicantType, Long.parseLong(fullAmount));
            // для юр.лиц скидочная сумма совпадает с полной, но не содержит нулей после умножения на 100 (на вход передаем умноженное на 100 fullAmount)
            // поэтому сравнение с ответом делаем всегда с saleAmount, чтобы избежать обратного деления.
            BigDecimal amountDecimal = new BigDecimal(saleAmount);
            return unusedPayments.stream().anyMatch(it -> Objects.compare(it.getAmount(), amountDecimal, Comparator.nullsLast(BigDecimal::compareTo)) == 0);
        } catch (ExternalServiceException | RestClientException e) {
            log.error("Не удалось получить ранее оплаченные пошлины", e);
        }
        return false;
    }
}
