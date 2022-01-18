package ru.gosuslugi.pgu.fs.component.payment.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.component.payment.PaymentTypeSelectorComponent;
import ru.gosuslugi.pgu.fs.component.payment.model.CommonDataBox;
import ru.gosuslugi.pgu.fs.service.process.impl.AbstractProcess;
import ru.gosuslugi.pgu.pgu_common.payment.dto.pay.PaymentPossibilityRequest;
import ru.gosuslugi.pgu.pgu_common.payment.dto.pay.PaymentPossibilityResponse;

import java.util.Optional;

import static ru.gosuslugi.pgu.pgu_common.payment.dto.pay.PaymentPossibilityResponse.PaymentPossibilityRequestState.SUCCESS;

@Service
@RequiredArgsConstructor
public class PaymentSelectorProcess extends AbstractProcess<PaymentSelectorProcess, ScenarioDto> {

    private final BillContainerService billContainerStrategy;
    private final PaymentTypeSelectorComponent payComponent;

    private ScenarioDto scenarioDto;
    private FieldComponent component;

    public PaymentSelectorProcess of(FieldComponent component, ScenarioDto scenarioDto) {
        init();
        this.component = component;
        this.scenarioDto = scenarioDto;
        return this;
    }

    @Override
    public PaymentSelectorProcess getProcess() {
        return this;
    }

    public boolean hasDataInDisplay(PaymentSelectorProcess process) {
        return scenarioDto.getDisplay().findFieldByComponentId(component.getId()).isPresent();
    }

    public PaymentSelectorProcess setAttrsFromDisplay(PaymentSelectorProcess process){
        Optional<FieldComponent> optionalField = scenarioDto.getDisplay().findFieldByComponentId(component.getId());
        optionalField.ifPresent(fieldComponent -> component.setAttrs(fieldComponent.getAttrs()));
        return this;
    }

    public boolean isSuccessUseBillContainer(PaymentSelectorProcess process) {
        return billContainerStrategy.applyBillContainer(component, scenarioDto, payComponent);
    }

    public PaymentSelectorProcess componentDefaultInit(PaymentSelectorProcess process) {
        CommonDataBox<PaymentPossibilityRequest, PaymentPossibilityResponse> box = payComponent.defaultInit(component, scenarioDto);
        PaymentPossibilityResponse result = box.getElementR();
        if (SUCCESS == result.getState()) {
            billContainerStrategy.refreshBillContainer(box);
        }
        return this;
    }

}
