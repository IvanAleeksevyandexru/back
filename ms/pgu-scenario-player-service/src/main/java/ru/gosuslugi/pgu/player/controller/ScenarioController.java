package ru.gosuslugi.pgu.player.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.gosuslugi.pgu.dto.InitServiceDto;
import ru.gosuslugi.pgu.dto.order.OrderInfoDto;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.ScenarioRequest;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.player.service.impl.LightweightScreenService;

/**
 * Controller that handle all methods for scenarios
 * Next/prev pages
 */
@RestController
@RequestMapping(value = "service/{serviceId}/scenario", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ScenarioController {

    private final LightweightScreenService lightweightScreenService;

    @PostMapping(value = "/getService")
    public ScenarioResponse getServiceInitScreen(@PathVariable String serviceId, @RequestBody InitServiceDto initServiceDto) {
        ScenarioResponse scenarioResponse = lightweightScreenService.getInitScreen(serviceId);

        ScenarioDto scenarioDto = scenarioResponse.getScenarioDto();
        scenarioDto.setServiceCode(serviceId);
        scenarioDto.setServiceDescriptorId(serviceId);
        scenarioDto.setTargetCode(initServiceDto.getTargetId());

        return scenarioResponse;
    }

    @PostMapping(value = "/getNextStep")
    public ScenarioResponse getNextStep(@PathVariable String serviceId, @RequestBody ScenarioRequest request) {
        return lightweightScreenService.getNextScreen(request, serviceId);
    }

    @PostMapping(value = "/getPrevStep")
    public ScenarioResponse getPrevStep(@PathVariable String serviceId, @RequestBody ScenarioRequest request, @RequestParam(defaultValue = "1") Integer stepsBack) {
        return lightweightScreenService.getPrevScreen(request, serviceId, stepsBack);
    }

    @PostMapping(value = "checkIfOrderIdExists")
    public OrderInfoDto checkIfOrderIdExists(@PathVariable String serviceId, @RequestBody InitServiceDto initServiceDto) {

        OrderInfoDto orderInfo = new OrderInfoDto();

        return orderInfo;
    }

}
