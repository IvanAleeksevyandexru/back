package ru.gosuslugi.pgu.fs.component.payment.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.component.payment.PaymentTypeSelectorComponent;
import ru.gosuslugi.pgu.fs.component.payment.model.BillContainer;
import ru.gosuslugi.pgu.fs.component.payment.model.CommonDataBox;
import ru.gosuslugi.pgu.pgu_common.payment.dto.pay.PaymentPossibilityRequest;
import ru.gosuslugi.pgu.pgu_common.payment.dto.pay.PaymentPossibilityResponse;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.gosuslugi.pgu.components.ComponentAttributes.ORGANIZATION_ID_ARG_KEY;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillContainerService {

    public static final String BILL_CONTAINER_ID = "billContainer";
    public static final String AMOUNT_CODE = "amountCode";

    public boolean applyBillContainer(FieldComponent component, ScenarioDto scenarioDto, PaymentTypeSelectorComponent paymentTypeSelectorComponent) {
        Optional<BillContainer> paymentDataBox = findBillContainer(component, scenarioDto);
        if (paymentDataBox.isPresent()) {
            var billContainer = paymentDataBox.get();
            PaymentPossibilityRequest request = PaymentPossibilityRequest.builder()
                    .applicantType(billContainer.getPaymentResponse().getApplicantType())
                    .orderId(billContainer.getOrderId())
                    .build();
            paymentTypeSelectorComponent.processResult(component, request, billContainer.getPaymentResponse(), billContainer.getServiceId());
            return true;
        }
        return false;
    }

    public void refreshBillContainer(CommonDataBox<PaymentPossibilityRequest, PaymentPossibilityResponse> box) {
        PaymentPossibilityRequest request = box.getElementT();
        PaymentPossibilityResponse response = box.getElementR();
        ScenarioDto scenarioDto = box.getScenario();

        BillContainer billData = BillContainer.builder()
                .amountCodes(request.getAmountCodes())
                .organizationId(request.getOrganizationId())
                .orderId(request.getOrderId())
                .serviceId(request.getServiceId())
                .serviceCode(request.getServiceCode())
                .paymentResponse(response)
                .build();
        ApplicantAnswer answer = new ApplicantAnswer(false, JsonProcessingUtil.toJson(billData));
        scenarioDto.getCachedAnswers().put(BILL_CONTAINER_ID, answer);
    }

    private Optional<BillContainer> findBillContainer(FieldComponent component, ScenarioDto scenarioDto) {
        var container = scenarioDto.getCachedAnswers().get(BILL_CONTAINER_ID);
        if (Objects.isNull(container)) return Optional.empty();

        var arguments = component.getArguments();
        List<String> amountCodes = IntStream.rangeClosed(1, 20)
                .mapToObj(el -> arguments.get(AMOUNT_CODE + el))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        String organizationId = component.getArgument(ORGANIZATION_ID_ARG_KEY);
        BillContainer billData = JsonProcessingUtil.fromJson(container.getValue(), BillContainer.class);
        BillContainer latestBillData = BillContainer.builder()
                .amountCodes(amountCodes)
                .organizationId(organizationId)
                .build();

        if (billData.hasDifferences(latestBillData)){
            scenarioDto.getCachedAnswers().remove(BILL_CONTAINER_ID);
            return Optional.empty();
        }
        return Optional.of(billData);
    }

}
