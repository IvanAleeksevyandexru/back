package ru.gosuslugi.pgu.fs.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.gosuslugi.pgu.draft.DraftClient;
import ru.gosuslugi.pgu.dto.InitServiceDto;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.ScenarioRequest;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.fs.common.descriptor.DescriptorService;
import ru.gosuslugi.pgu.fs.delirium.client.DeliriumClientStub;
import ru.gosuslugi.pgu.fs.delirium.model.DeliriumStageDto;
import ru.gosuslugi.pgu.fs.pgu.service.impl.PguOrderServiceStub;
import ru.gosuslugi.pgu.fs.service.custom.MainScreenServiceRegistry;
import ru.gosuslugi.pgu.fs.service.timer.impl.TimerClientStub;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Test controller used for debug only in test environment
 * to  be removed
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class TestDemoController {
    private final MainScreenServiceRegistry mainScreenServiceRegistry;
    private final DescriptorService descriptorService;
    private final DraftClient draftClient;
    private final Environment environment;

    /**
     * Test method for updating service description json
     */
    @Operation(summary = "Позволяет переопределить JSON услуги в целях тестирования")
    @RequestMapping(value = "/setService/{serviceId}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public void uploadSpecification(@Parameter(description = "Id услуги") @PathVariable String serviceId, @RequestBody ServiceDescriptor serviceDescriptor) {
        descriptorService.applyNewServiceDescriptor(serviceId, serviceDescriptor);
    }

    /**
     * Test method for updating order id
     * @see PguOrderServiceStub#EXISTING_ORDER_ID
     */
    @Operation(summary = "Устанавливает текущий ID заявления, если отключена интеграция с ЛК (`orderid.integration = false`)")
    @PutMapping(value = "/test/orderId/{orderId}")
    public void updateOrderId(@Parameter(description = "Id заявления") @PathVariable String orderId) {
        if(log.isInfoEnabled()) log.info("updating EXISTING_ORDER_ID to {}", orderId);
        PguOrderServiceStub.EXISTING_ORDER_ID = Long.parseLong(orderId);
    }

    /**
     * Test method for updating stage in delirium stub
     * @see DeliriumStageDto
     * @see DeliriumClientStub#TEST_STAGE_DTO
     */
    @Operation(summary = "Устанавливает текущий этап в Delirium, если отключена интеграция с Delirium (`delirium.enabled = false`)")
    @PutMapping(path = "/test/deliriumStage", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateDeliriumStage(@RequestBody DeliriumStageDto deliriumStageDto) {
        if(log.isInfoEnabled()) log.info("updating stage to {}", deliriumStageDto);
        DeliriumClientStub.TEST_STAGE_DTO = deliriumStageDto;
    }

    /**
     * Test method for updating timer in timer stub
     * @see TimerClientStub#DURATION
     * @see TimerClientStub#START_TIME
     * @param timerAttrs атрибуты (строка) duration, startTime
     */
    @Operation(summary = "Устанавливает таймер, если отключена интеграция с сервисом таймеров (`timer-service.integration = false`)")
    @PutMapping(path = "/test/timer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateTimer(@RequestBody Map<String, String> timerAttrs) {
        if(log.isInfoEnabled()) log.info("updating timer to {}", timerAttrs);
        TimerClientStub.DURATION = Integer.parseInt(timerAttrs.get("duration"));
        TimerClientStub.START_TIME = timerAttrs.get("startTime");
    }

    /**
     * Test method for getting first service step
     * same as in ScenarioController (getInitStep)
     */
    @Operation(summary = "Возвращает первый шаг услуги")
    @RequestMapping(value = "/getService/{serviceId}", method = RequestMethod.GET, produces = "application/json")
    public ScenarioResponse getScreen(@Parameter(description = "Id услуги") @PathVariable String serviceId) {
        return mainScreenServiceRegistry.getService(serviceId).getInitScreen(serviceId, new InitServiceDto());
    }

    /**
     * Test method for setting scenario
     */
    @Operation(summary = "Позволяет переопределить черновик услуги", description = "Работает только в при включенном профиле `local`")
    @RequestMapping(value = "/test/setScenario", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public void setScenario(@RequestBody ScenarioRequest scenarioRequest) {
        if (Stream.of(environment.getActiveProfiles()).anyMatch(profile -> profile.equalsIgnoreCase("local"))) {
            ScenarioDto scenario = scenarioRequest.getScenarioDto();
            String serviceId = scenario.getServiceCode();
            ServiceDescriptor serviceDescriptor = descriptorService.getServiceDescriptor(serviceId);

            draftClient.deleteDraft(PguOrderServiceStub.EXISTING_ORDER_ID, null);
            PguOrderServiceStub.EXISTING_ORDER_ID = scenario.getOrderId();
            draftClient.saveDraft(scenario, serviceId, null, null, serviceDescriptor.getDraftTtl(), serviceDescriptor.getOrderTtl());
        }
    }
}
