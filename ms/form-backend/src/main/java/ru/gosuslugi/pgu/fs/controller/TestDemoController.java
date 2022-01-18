package ru.gosuslugi.pgu.fs.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.gosuslugi.pgu.dto.InitServiceDto;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.ScenarioRequest;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.draft.DraftClient;
import ru.gosuslugi.pgu.fs.delirium.client.DeliriumClientStub;
import ru.gosuslugi.pgu.fs.delirium.model.DeliriumStageDto;
import ru.gosuslugi.pgu.fs.common.descriptor.DescriptorService;
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
    @RequestMapping(value = "/setService/{serviceId}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public void uploadSpecification(@PathVariable String serviceId, @RequestBody ServiceDescriptor serviceDescriptor) {
        descriptorService.applyNewServiceDescriptor(serviceId, serviceDescriptor);
    }

    /**
     * Test method for updating order id
     * @see PguOrderServiceStub#EXISTING_ORDER_ID
     */
    @GetMapping(value = "/test/orderId/{orderId}")
    public void updateOrderId(@PathVariable String orderId) {
        if(log.isInfoEnabled()) log.info("updating EXISTING_ORDER_ID to {}", orderId);
        PguOrderServiceStub.EXISTING_ORDER_ID = Long.parseLong(orderId);
    }

    /**
     * Test method for updating stage in delirium stub
     * @see DeliriumStageDto
     * @see DeliriumClientStub#TEST_STAGE_DTO
     */
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
    @RequestMapping(value = "/getService/{serviceId}", method = RequestMethod.GET, produces = "application/json")
    public ScenarioResponse getScreen(@PathVariable String serviceId) {
        return mainScreenServiceRegistry.getService(serviceId).getInitScreen(serviceId, new InitServiceDto());
    }

    /**
     * Test method for setting user token
     */
    @RequestMapping(value = "/setPd", method = RequestMethod.GET, produces = "application/json")
    public String setTestPD(@RequestParam("uuid") String uuid, @RequestParam("token") String token) {
        //personalDataService.setPersonToken(uuid, token);
        return "Ok";
    }

    /**
     * Test method for getting user data
     */
    @RequestMapping(value = "/getPd", method = RequestMethod.GET, produces = "application/json")
    public String getTestPD(@RequestParam("uuid") String uuid, @RequestParam("token") String token) {
        return "Ok";
    }

    /**
     * Test method for setting scenario
     */
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
