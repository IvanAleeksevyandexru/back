package ru.gosuslugi.pgu.fs.component.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.component.payment.model.BillInfoComponentDto;
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.BillInfo;
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.BillInfoAttr;
import ru.gosuslugi.pgu.pgu_common.payment.dto.bill.BillInfoResponseWrapper;
import ru.gosuslugi.pgu.pgu_common.payment.service.BillingService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.gosuslugi.pgu.components.ComponentAttributes.*;
import static ru.gosuslugi.pgu.components.FieldComponentUtil.ACTIONS_ATTR_KEY;

/**
 * Компонент отображения информации об оплаченном счете
 */
@Component
@RequiredArgsConstructor
public class BillInfoComponent extends AbstractComponent<BillInfoComponentDto> {

    private static final String DOWNLOAD_PDF_ACTION_TYPE = "download";
    private static final String ORIGINAL_AMOUNT_BILL_ATTR = "OriginalAmount";


    private final BillingService billingService;
    private final UserPersonalData userPersonalData;

    @Override
    public ComponentType getType() {
        return ComponentType.BillInfo;
    }

    @Override
    public ComponentResponse<BillInfoComponentDto> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        String billId = component.getArgument(BILL_ID_ATTR);
        BillInfoResponseWrapper billInfoResponseWrapper = billingService.getBillInfo(userPersonalData.getToken(), billId);
        List<BillInfo> bills = billInfoResponseWrapper.getResponse().getBills();
        if (CollectionUtils.isEmpty(bills)) {
            return ComponentResponse.empty();
        }
        BillInfo billInfo = bills.get(0);
        if (billingService.isBillPaid(billInfoResponseWrapper)) {
            List<Map<String, String>> actions = FieldComponentUtil.getStringList(component, ACTIONS_ATTR_KEY, true);
            actions = actions.stream().filter(action -> !DOWNLOAD_PDF_ACTION_TYPE.equalsIgnoreCase(action.get("type"))).collect(Collectors.toList());
            component.getAttrs().put(ACTIONS_ATTR_KEY, actions);
        }
        String amount = billInfo.getAddAttrs().stream()
                .filter(attr -> ORIGINAL_AMOUNT_BILL_ATTR.equals(attr.getName()))
                .findAny()
                .map(BillInfoAttr::getValue)
                .orElse(String.valueOf(billInfo.getAmount()));

        BillInfoComponentDto billInfoComponentDto = new BillInfoComponentDto();
        billInfoComponentDto.setBillName(billInfo.getBillName());
        billInfoComponentDto.setBillNumber(billInfo.getBillNumber());
        billInfoComponentDto.setBillDate(billInfo.getBillDate());
        billInfoComponentDto.setAmount(amount);
        billInfoComponentDto.setBillId(billInfo.getBillId());

        return ComponentResponse.of(billInfoComponentDto);
    }

}
