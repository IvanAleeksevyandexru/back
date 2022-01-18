package ru.gosuslugi.pgu.fs.component.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.component.payment.model.BillInfoComponentDto;
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.BillInfo;
import ru.gosuslugi.pgu.pgu_common.payment.dto.PaymentDetailsDto;
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.BillInfoAttr;
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.BillInfoResponse;
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.BillInfoResponseWrapper;
import ru.gosuslugi.pgu.pgu_common.payment.service.BillingService;
import ru.gosuslugi.pgu.pgu_common.payment.service.PaymentService;

import java.util.*;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static ru.gosuslugi.pgu.components.ComponentAttributes.BILL_ID_ATTR;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentComponent extends AbstractComponent<BillInfoComponentDto> {

    @Value("${mock.payment.skip}")
    private boolean skip;

    private static final String ORIGINAL_AMOUNT = "OriginalAmount";
    private static final String DEFAULT_VALUE_FOR_ORIGINAL_AMOUNT = "";
    private static final String CHECK_ONLY_UIN_EXISTING = "goNextAfterUIN";

    private final PaymentService paymentService;
    private final UserPersonalData userPersonalData;
    private final BillingService billingService;

    @Override
    public ComponentType getType() {
        return ComponentType.PaymentScr;
    }

    @Override
    public ComponentResponse<BillInfoComponentDto> getInitialValue(FieldComponent component) {
        String buildId = component.getArgument(BILL_ID_ATTR);
        BillInfoResponseWrapper responseWrapper = billingService.getBillInfo(userPersonalData.getToken(), buildId);
        if(nonNull(responseWrapper) && nonNull(responseWrapper.getResponse())){
            BillInfoResponse response = responseWrapper.getResponse();
            if(nonNull(response.getBills()) && !response.getBills().isEmpty()){
                BillInfo billInfo = response.getBills().get(0);
                BillInfoComponentDto billInfoComponentDto = new BillInfoComponentDto();
                billInfoComponentDto.setBillName(billInfo.getBillName());
                billInfoComponentDto.setBillNumber(billInfo.getBillNumber());
                billInfoComponentDto.setBillDate(billInfo.getBillDate());
                billInfoComponentDto.setAmount(String.valueOf(billInfo.getAmount()));
                billInfoComponentDto.setBillId(billInfo.getBillId());
                billInfoComponentDto.setOriginalAmount(getOriginalAmount(billInfo));
                return ComponentResponse.of(billInfoComponentDto);
            }
        }
        return ComponentResponse.empty();
    }

    private String getOriginalAmount(BillInfo billInfo) {
        if (!CollectionUtils.isEmpty(billInfo.getAddAttrs())) {
            return billInfo.getAddAttrs().stream()
                    .filter(Objects::nonNull)
                    .filter(billInfoAttr -> ORIGINAL_AMOUNT.equalsIgnoreCase(billInfoAttr.getName()))
                    .map(BillInfoAttr::getValue)
                    .filter(StringUtils::hasText)
                    .findFirst()
                    .orElse(DEFAULT_VALUE_FOR_ORIGINAL_AMOUNT);
        }
        return DEFAULT_VALUE_FOR_ORIGINAL_AMOUNT;
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        if (skip) {
            return;
        }
        // https://jira.egovdev.ru/browse/EPGUCORE-45938 - если true, то только проверить наличие UIN, оплаты еще не было
        if (!CollectionUtils.isEmpty(fieldComponent.getAttrs()) && Boolean.TRUE.equals(fieldComponent.getAttrs().get(CHECK_ONLY_UIN_EXISTING))) {
            PaymentDetailsDto paymentDetailsDto = updatePaymentDetails(entry, fieldComponent, null);
            if (!StringUtils.hasText(paymentDetailsDto.getUin())) {
                incorrectAnswers.put(fieldComponent.getId(), "Отсутствует UIN");
            }
            return;
        }
        String billId = fieldComponent.getArguments().getOrDefault(BILL_ID_ATTR, "");
        BillInfoResponseWrapper paymentStatus = billingService.getBillInfo(userPersonalData.getToken(), billId);
        if (
                paymentStatus == null
                || isNull(paymentStatus.getError())
                || isNull(paymentStatus.getError().getCode())
                || (paymentStatus.getError().getCode() != 0)
                || isNull(paymentStatus.getResponse())
                || isNull(paymentStatus.getResponse().getBills())
                || paymentStatus.getResponse().getBills().isEmpty()
        ) {
            incorrectAnswers.put(fieldComponent.getId(), "Оплата должна быть произведена");
            return;
        }

        updatePaymentDetails(entry, fieldComponent, paymentStatus.getResponse().getBills().get(0).getBillNumber());
    }

    private PaymentDetailsDto updatePaymentDetails(Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent, String uin) {
        PaymentDetailsDto paymentDetailsDto = JsonProcessingUtil.fromJson(entry.getValue().getValue(), PaymentDetailsDto.class);
        // меняем так как PRIOR0316373326122020005395386 превращается в 0316373326122020005395386
        String checkedUin = uin;
        if (!StringUtils.hasText(checkedUin) && StringUtils.hasText(paymentDetailsDto.getUin())) {
            checkedUin = paymentDetailsDto.getUin().replace("PRIOR", "");
        }
        if (!StringUtils.hasText(checkedUin)) {
            return paymentDetailsDto;
        }
        paymentDetailsDto.setUin(checkedUin);
        String answerValue = JsonProcessingUtil.toJson(paymentDetailsDto);
        fieldComponent.setValue(answerValue);
        var answer = entry.getValue();
        answer.setValue(answerValue);
        entry.setValue(answer);
        return paymentDetailsDto;
    }

    private String getPaymentCode(FieldComponent fieldComponent) {
        return Optional.ofNullable(fieldComponent.getAttrs())
                .orElse(Collections.emptyMap())
                .getOrDefault("payCode", "1")
                .toString();
    }

}
