package ru.gosuslugi.pgu.fs.component.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.component.validation.NotBlankValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry;
import ru.gosuslugi.pgu.fs.common.variable.VariableType;
import ru.gosuslugi.pgu.pgu_common.payment.dto.PaymentInfo;
import ru.gosuslugi.pgu.pgu_common.payment.service.PaymentService;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.gosuslugi.pgu.components.ComponentAttributes.FULL_AMOUNT_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.IGNORE_ORGCODE;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ORGANIZATION_ID_ARG_KEY;
import static ru.gosuslugi.pgu.components.ComponentAttributes.USE_PAYMENT_INFO_API_V1;
import static ru.gosuslugi.pgu.components.ComponentAttributes.SALE_AMOUNT_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.REUSE_PAYMENT_UIN;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnusedPaymentsComponent extends AbstractComponent<List<PaymentInfo>> {

    private static final String APPLICANT_TYPE_ARG_KEY = "applicantType";

    public static final String ARGUMENT_PASSPORT_TC = "passportTC";
    public static final String ARGUMENT_REGISTRATION_TYPE = "registrationType";
    public static final String ARGUMENT_GRZ_YES = "grzYes";

    private final PaymentService paymentService;
    private final JsonProcessingService jsonProcessingService;
    private final UserPersonalData userPersonalData;
    private final VariableRegistry variableRegistry;

    @Override
    public ComponentType getType() {
        return ComponentType.UnusedPayments;
    }

    @Override
    public ComponentResponse<List<PaymentInfo>> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        String applicantType = getApplicantType(component);
        List<PaymentInfo> payments = getPayments(component, scenarioDto, applicantType);
        String saleAmountStr = component.getArgument(SALE_AMOUNT_ATTR);
        if (!StringUtils.isEmpty(saleAmountStr)) {
            BigDecimal amount = new BigDecimal(saleAmountStr);
            return ComponentResponse.of(payments.stream()
                    .filter(order -> Objects.compare(order.getAmount(), amount, Comparator.nullsLast(BigDecimal::compareTo)) == 0)
                    .collect(Collectors.toList()));
        }
        return ComponentResponse.empty();
    }

    @Override
    public List<ValidationRule> getValidations() {
        return List.of(
                new NotBlankValidation("Выберите платёж")
        );
    }

    @Override
    protected void postProcess(Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        String entryValue = AnswerUtil.getValue(entry);
        PaymentInfo selectedPayment = jsonProcessingService.fromJson(entryValue, PaymentInfo.class);
        scenarioDto.getCachedAnswers().put(REUSE_PAYMENT_UIN, new ApplicantAnswer(true, selectedPayment.getUin()));
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        String entryValue = AnswerUtil.getValue(entry);
        if (StringUtils.hasText(entryValue)) {
            PaymentInfo selectedPayment = jsonProcessingService.fromJson(entryValue, PaymentInfo.class);
            String applicantType = getApplicantType(fieldComponent);
            List<PaymentInfo> ordersInfo = getPayments(fieldComponent, scenarioDto, applicantType);
            if (ordersInfo.stream().noneMatch(payment -> payment.equals(selectedPayment))) {
                incorrectAnswers.put(fieldComponent.getId(), "Выбранный платёж недоступен");
            }
        }
    }

    private List<PaymentInfo> getPayments(FieldComponent component, ScenarioDto scenarioDto, String applicantType) {
        Long orderId = scenarioDto.getOrderId();
        String serviceId = variableRegistry.getVariable(VariableType.serviceId).getValue(scenarioDto);
        long amount = Long.parseLong(component.getArgument(FULL_AMOUNT_ATTR));
        String value = component.getArgument(IGNORE_ORGCODE);
        boolean ignoreOrgCode = Boolean.parseBoolean(value);
        String orgCode = ignoreOrgCode ? null : component.getArgument(ORGANIZATION_ID_ARG_KEY);
        String token = userPersonalData.getToken();

        // в случае присутствия флага usePaymentInfoApiV1 - используем api v1
        boolean isUsePaymentInfoApiV1 = Boolean.parseBoolean(Objects.toString(component.getAttrs().get(USE_PAYMENT_INFO_API_V1)));
        if (isUsePaymentInfoApiV1) {
            String passportTC = component.getArgument(ARGUMENT_PASSPORT_TC);
            String registrationType = component.getArgument(ARGUMENT_REGISTRATION_TYPE);
            String grzYes = component.getArgument(ARGUMENT_GRZ_YES);
            return paymentService.getUnusedPaymentsV1(orderId, orgCode, token, serviceId, passportTC, registrationType, grzYes);
        }

        // реализация по-умолчанию использует api v3
        return paymentService.getUnusedPaymentsV3(orderId, orgCode, token, serviceId, applicantType, amount);
    }

    private String getApplicantType(FieldComponent component) {
        String applicantType = component.getArgument(APPLICANT_TYPE_ARG_KEY);
        applicantType = applicantType.isEmpty() ? "FL" : applicantType;
        return applicantType;
    }
}
