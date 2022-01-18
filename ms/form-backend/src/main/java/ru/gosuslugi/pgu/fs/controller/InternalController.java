package ru.gosuslugi.pgu.fs.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.gosuslugi.pgu.dto.ScenarioRequest;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.fs.service.SubScreenService;
import ru.gosuslugi.pgu.fs.utils.TracingHelper;

@RestController
@RequestMapping(value = "/internal/scenario", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class InternalController {

    private final SubScreenService screenService;
    private final TracingHelper tracingHelper;

    @PostMapping(value = "/getNextStep", produces = "application/json; charset=UTF-8")
    public ScenarioResponse getNextStep(@RequestBody ScenarioRequest request) {
        tracingHelper.addServiceCodeAndOrderId(request);
        String serviceId = request.getScenarioDto().getServiceId();
        return screenService.getNextScreen(request, serviceId);
    }

    @PostMapping(value = "/getPrevStep")
    public ScenarioResponse getPrevStep(@RequestBody ScenarioRequest request, @RequestParam(defaultValue = "1") Integer stepsBack) {
        tracingHelper.addServiceCodeAndOrderId(request);
        String serviceId = request.getScenarioDto().getServiceId();
        return screenService.getPrevScreen(request, serviceId, stepsBack);
    }

}
