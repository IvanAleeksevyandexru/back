package ru.gosuslugi.pgu.fs.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.logging.service.SpanService;
import ru.gosuslugi.pgu.dto.InitServiceDto;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.ScenarioRequest;
import ru.gosuslugi.pgu.dto.action.ActionRequestDto;

import static java.util.Objects.nonNull;
import static org.springframework.util.StringUtils.hasText;

@Component
@RequiredArgsConstructor
public class TracingHelper {

    private final SpanService spanService;

    public void addServiceCodeAndOrderId(ScenarioRequest request) {
        if (nonNull(request)) {
            if (nonNull(request.getCallBackOrderId())) {
                spanService.addTagToSpan(SpanService.ORDER_ID_TAG, String.valueOf(request.getCallBackOrderId()));
            }
            if (nonNull(request.getCallBackServiceId())) {
                spanService.addTagToSpan(SpanService.SERVICE_CODE_TAG, request.getCallBackServiceId());
            }
        }
    }

    public void addServiceCodeAndOrderId(String serviceCode, ScenarioRequest request) {
        if (hasText(serviceCode)) {
            spanService.addTagToSpan(SpanService.SERVICE_CODE_TAG, serviceCode);
        }
        if (nonNull(request) && nonNull(request.getCallBackOrderId())) {
            spanService.addTagToSpan(SpanService.ORDER_ID_TAG, String.valueOf(request.getCallBackOrderId()));
        }
    }

    public void addServiceCodeAndOrderId(String serviceCode, ScenarioDto scenarioDto) {
        if (hasText(serviceCode)) {
            spanService.addTagToSpan(SpanService.SERVICE_CODE_TAG, serviceCode);
        }
        if (nonNull(scenarioDto) && nonNull(scenarioDto.getOrderId())) {
            spanService.addTagToSpan(SpanService.ORDER_ID_TAG, String.valueOf(scenarioDto.getOrderId()));
        }
    }

    public void addServiceCodeAndOrderId(ActionRequestDto request) {
        if (nonNull(request) && nonNull(request.getScenarioDto())) {
            if (nonNull(request.getScenarioDto().getOrderId())) {
                spanService.addTagToSpan(SpanService.ORDER_ID_TAG, String.valueOf(request.getScenarioDto().getOrderId()));
            }
            if (nonNull(request.getScenarioDto().getServiceCode())) {
                spanService.addTagToSpan(SpanService.SERVICE_CODE_TAG, request.getScenarioDto().getServiceCode());
            }
        }
    }

    public void addServiceCodeAndOrderId(String serviceCode, InitServiceDto initServiceDto) {
        if (hasText(serviceCode)) {
            spanService.addTagToSpan(SpanService.SERVICE_CODE_TAG, serviceCode);
        }
        if (nonNull(initServiceDto) && nonNull(initServiceDto.getOrderId())) {
            spanService.addTagToSpan(SpanService.ORDER_ID_TAG, String.valueOf(initServiceDto.getOrderId()));
        }
    }
}
