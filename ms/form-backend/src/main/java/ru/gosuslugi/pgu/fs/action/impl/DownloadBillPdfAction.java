package ru.gosuslugi.pgu.fs.action.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.DisplayRequest;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.action.ActionRequestDto;
import ru.gosuslugi.pgu.dto.action.ActionResponseDto;
import ru.gosuslugi.pgu.fs.action.ActionService;
import ru.gosuslugi.pgu.fs.action.ActionType;
import ru.gosuslugi.pgu.pgu_common.payment.service.BillingService;

import static ru.gosuslugi.pgu.components.ComponentAttributes.BILL_ID_ATTR;

@Component
@RequiredArgsConstructor
public class DownloadBillPdfAction implements ActionService {

    private final BillingService billingService;

    @Override
    public ActionType getActionType() {
        return ActionType.downloadBillPdf;
    }

    @Override
    public ActionResponseDto invoke(ActionRequestDto actionRequestDto) {
        ScenarioDto scenarioDto = actionRequestDto.getScenarioDto();
        DisplayRequest display = scenarioDto.getDisplay();
        String billId = display.getComponents().stream()
                .filter(component -> !component.getArgument(BILL_ID_ATTR).isEmpty())
                .map(component -> component.getArgument(BILL_ID_ATTR))
                .findAny().orElse("");
        String billPdfURI = billingService.getBillPdfURI(billId);
        ActionResponseDto responseDto = new ActionResponseDto();
        responseDto.getResponseData().put("value", billPdfURI);
        responseDto.getResponseData().put("type", MediaType.APPLICATION_PDF_VALUE);
        return responseDto;
    }
}
