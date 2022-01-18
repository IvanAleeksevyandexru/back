package ru.gosuslugi.pgu.fs.component.payment;

import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.component.payment.model.ChildrenClubsGroup;
import ru.gosuslugi.pgu.fs.component.payment.model.ChildrenClubsProgram;
import ru.gosuslugi.pgu.fs.component.payment.model.ChildrenClubsValue;
import ru.gosuslugi.pgu.fs.component.payment.model.FinancialSource;
import ru.gosuslugi.pgu.fs.component.payment.model.FinancialSourceBudget;
import ru.gosuslugi.pgu.fs.component.payment.model.PaymentWayDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.gosuslugi.pgu.components.ComponentAttributes.VALUE_ATTR;
import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.PaymentWay;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentWayComponent extends AbstractComponent<List<PaymentWayDto>> {
    private static final String FINANCE_TYPE_NONE = "none";
    private static final String FINANCE_TYPE_BUDGET = "budget";
    private static final String FINANCE_TYPE_PAID = "paid";
    private static final String FINANCE_TYPE_CERT = "pfdod_certificate";
    private static final String FINANCE_TYPE_PRIVATE = "private";
    private static final Set<String> FINANCIAL_TYPES = Set.of(FINANCE_TYPE_NONE, FINANCE_TYPE_BUDGET, FINANCE_TYPE_PAID, FINANCE_TYPE_CERT, FINANCE_TYPE_PRIVATE);
    private static final String PAYMENT_WAYS = "paymentWays";
    public static final String HTML_ATTR_ROOT = "html";

    @Override
    public ComponentType getType() {
        return PaymentWay;
    }

    @Override
    public ComponentResponse<List<PaymentWayDto>> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        ChildrenClubsValue childrenClubsValue = getChildrenClubsValue(component);

        List<PaymentWayDto> paymentWays = getPaymentWays(childrenClubsValue);
        component.getAttrs().put(PAYMENT_WAYS, paymentWays);
        return ComponentResponse.empty();
    }

    private List<PaymentWayDto> getPaymentWays(ChildrenClubsValue childrenClubsValue) {
        Optional<ChildrenClubsGroup> childrenClubsGroup = Optional.ofNullable(childrenClubsValue).map(ChildrenClubsValue::getGroup);
        FinancialSource financialSource = childrenClubsGroup.map(ChildrenClubsGroup::getFinancialSource).orElse(null);
        FinancialSourceBudget financialSourceBudget = childrenClubsGroup.map(ChildrenClubsGroup::getFinancialSourceBudget).orElse(null);
        if (financialSource == null || financialSourceBudget == null) {
            throw new FormBaseException("Ошибка парсинга объекта ChildrenClubsValue");
        }

        String programType = Optional.of(childrenClubsValue).map(ChildrenClubsValue::getProgram).map(ChildrenClubsProgram::getTypeOfBudget).orElse(null);

        Optional<PaymentWayDto> freePaymentWay = getFreePaymentWay(financialSource, programType);
        Optional<PaymentWayDto> certificatePaymentWay = getCertificatePaymentWay(financialSource, financialSourceBudget);
        Optional<PaymentWayDto> privatePaymentWay = getPrivatePaymentWay(financialSource, financialSourceBudget);

        return Stream.of(certificatePaymentWay, privatePaymentWay, freePaymentWay).
                filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<PaymentWayDto> getCertificatePaymentWay(FinancialSource financialSource, FinancialSourceBudget financialSourceBudget) {
        Optional<PaymentWayDto> optional = Optional.empty();
        if (Boolean.TRUE.equals(financialSource.getPfdodCertificate())) {
            PaymentWayDto certificate = new PaymentWayDto(FINANCE_TYPE_CERT, financialSourceBudget.getPfdodCertificate(), null);
            optional = Optional.of(certificate);
        }
        return optional;
    }

    private Optional<PaymentWayDto> getPrivatePaymentWay(FinancialSource financialSource, FinancialSourceBudget financialSourceBudget) {
        Optional<PaymentWayDto> optional = Optional.empty();
        if (Boolean.TRUE.equals(financialSource.getPrivateSource())) {
            PaymentWayDto privatePaid = new PaymentWayDto(FINANCE_TYPE_PRIVATE, financialSourceBudget.getPrivateSource(), null);
            optional = Optional.of(privatePaid);
        } else if (Boolean.TRUE.equals(financialSource.getPaid())) {
            PaymentWayDto paid = new PaymentWayDto(FINANCE_TYPE_PAID, financialSourceBudget.getPaid(), null);
            optional = Optional.of(paid);
        }
        return optional;
    }

    private Optional<PaymentWayDto> getFreePaymentWay(FinancialSource financialSource, String programType) {
        Optional<PaymentWayDto> optional = Optional.empty();
        if (Boolean.TRUE.equals(financialSource.getNone())) {
            PaymentWayDto freeNone = new PaymentWayDto(FINANCE_TYPE_NONE, null, programType);
            optional = Optional.of(freeNone);
        } else if (Boolean.TRUE.equals(financialSource.getBudget())) {
            PaymentWayDto freeBudget = new PaymentWayDto(FINANCE_TYPE_BUDGET, null, programType);
            optional = Optional.of(freeBudget);
        }
        return optional;
    }

    /**
     * Получает объект значения ChildrenClub из json
     * @param component компонент
     * @return объект ChildrenClub
     */
    private ChildrenClubsValue getChildrenClubsValue(FieldComponent component) {
        String childrenClubStr = component.getArgument(VALUE_ATTR);
        if(StringUtils.isEmpty(childrenClubStr)) {
            throw new FormBaseException(String.format("В компоненте %s отсутствует аргумент %s в linkedValues", getType(), VALUE_ATTR));
        }
        return JsonProcessingUtil.fromJson(childrenClubStr, ChildrenClubsValue.class);
    }

    @Override
    protected void preProcess(FieldComponent component, ScenarioDto scenarioDto) {
        Map<String, Object> htmlObjRoot = (Map<String, Object>) component.getAttrs().get(HTML_ATTR_ROOT);

        FINANCIAL_TYPES.forEach(type -> {
            String htmlPropValue = (String) htmlObjRoot.get(type);
            String resolvedValue = componentReferenceService.getValueByContext(htmlPropValue, Function.identity(),
                componentReferenceService.buildPlaceholderContext(component, scenarioDto),
                componentReferenceService.getContexts(scenarioDto));
            htmlObjRoot.put(type, resolvedValue);
        });
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        String entryValue = AnswerUtil.getValue(entry);
        if (StringUtils.isEmpty(entryValue)) {
            throw new FormBaseException("Способ оплаты должен быть выбран");
        }
        if (!FINANCIAL_TYPES.contains(entryValue)) {
            throw new FormBaseException(String.format("Способ оплаты %s не соответствует ни одному допустимому", entryValue));
        }
    }
}
