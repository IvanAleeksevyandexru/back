package ru.gosuslugi.pgu.fs.action.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.action.ActionRequestDto;
import ru.gosuslugi.pgu.dto.action.ActionResponseDto;
import ru.gosuslugi.pgu.fs.action.ActionService;
import ru.gosuslugi.pgu.fs.action.ActionType;
import ru.gosuslugi.pgu.sp.adapter.SpAdapterClient;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DownloadTsReportPdfAction implements ActionService {

    private final UserPersonalData userPersonalData;
    private final SpAdapterClient spAdapterClient;

    @Override
    public ActionType getActionType() {
        return ActionType.downloadTsReportPdf;
    }

    @Override
    public ActionResponseDto invoke(ActionRequestDto actionRequestDto) {
        var orderId = actionRequestDto.getScenarioDto().getOrderId();
        byte[] response = spAdapterClient.getTsReportPdf(orderId, userPersonalData);
        return response != null ? getResponse(response) : null;
    }

    private ActionResponseDto getResponse(byte[] value){
        var response = new ActionResponseDto();
        Map<String, Object> responseParams = new HashMap<>();
        responseParams.put("value", value);
        responseParams.put("type", "application/pdf;base64");
        response.setResponseData(responseParams);
        return response;
    }
}