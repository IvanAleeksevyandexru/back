package ru.gosuslugi.pgu.fs.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.gosuslugi.pgu.dto.*;
import ru.gosuslugi.pgu.common.core.service.HealthHolder;
import ru.gosuslugi.pgu.dto.order.OrderInfoDto;
import ru.gosuslugi.pgu.dto.order.OrderListInfoDto;
import ru.gosuslugi.pgu.fs.common.controller.ScenarioApi;
import ru.gosuslugi.pgu.fs.service.DeliriumService;
import ru.gosuslugi.pgu.fs.service.MainScreenService;
import ru.gosuslugi.pgu.fs.service.custom.MainScreenServiceRegistry;
import ru.gosuslugi.pgu.fs.utils.TracingHelper;

import java.util.Map;
import java.util.Objects;

/**
 * Controller that handle all methods for scenarios
 * Next/prev pages
 */
@RestController
@RequestMapping(value = "service/{serviceId}/scenario", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ScenarioController implements ScenarioApi {

    private final DeliriumService deliriumService;
    private final HealthHolder healthHolder;
    private final TracingHelper tracingHelper;
    private final MainScreenServiceRegistry mainScreenServiceRegistry;

    /**
     * Method for getting first screen and  generating new orderId
     *
     * @return scenario response
     */
    @ApiOperation(value = "Инициализация услуги")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Неверные параметры"),
            @ApiResponse(code = 401, message = "Не авторизованный пользователь"),
            @ApiResponse(code = 403, message = "Нет прав"),
            @ApiResponse(code = 500, message = "Внутренняя ошибка")})
    @Override
    @PostMapping(value = "/getService")
    public ScenarioResponse getServiceInitScreen(@ApiParam(value = "Id услуги", required = true) @PathVariable String serviceId,
                                                 @ApiParam(value = "Дополнительная информация для инициализации услуги", required = true) @RequestBody InitServiceDto initServiceDto) {
        tracingHelper.addServiceCodeAndOrderId(serviceId, initServiceDto);
        MainScreenService mainScreenService = mainScreenServiceRegistry.getService(initServiceDto.getTargetId());
        if (Objects.isNull(initServiceDto.getOrderId())) {
            return mainScreenService.getInitScreen(serviceId, initServiceDto);
        }
        return mainScreenService.getExistingScenario(initServiceDto, serviceId);
    }

    /**
     * Method for for submit button handle
     */
    @Override
    @ApiOperation(value = "Получение следуещего шага в сценарии услуги")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Неверные параметры"),
            @ApiResponse(code = 401, message = "Не авторизованный пользователь"),
            @ApiResponse(code = 403, message = "Нет прав"),
            @ApiResponse(code = 500, message = "Внутренняя ошибка")})
    @PostMapping(value = "/getNextStep", produces = "application/json; charset=utf-8")
    public ScenarioResponse getNextStep(@ApiParam(value = "Id услуги", required = true) @PathVariable String serviceId,
                                        @ApiParam(value = "ScenarioRequest с текущим шагом сценария", required = true) @RequestBody ScenarioRequest request) {
        tracingHelper.addServiceCodeAndOrderId(serviceId, request);
        MainScreenService mainScreenService = mainScreenServiceRegistry.getService(request.getScenarioDto().getTargetCode());
        mainScreenService.setStatusId(request.getScenarioDto());
        ScenarioResponse result = mainScreenService.getNextScreen(request, serviceId);
        result.setHealth(healthHolder.get());
        return result;
    }

    @Override
    @ApiOperation(value = "Сохранение кешированных значений заявления в черновик")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Неверные параметры"),
            @ApiResponse(code = 401, message = "Не авторизованный пользователь"),
            @ApiResponse(code = 403, message = "Нет прав"),
            @ApiResponse(code = 500, message = "Внутренняя ошибка")})
    @PostMapping(value = "/saveCacheToDraft", produces = "application/json; charset=utf-8")
    public ScenarioResponse saveCacheToDraft(@ApiParam(value = "Id услуги", required = true) @PathVariable String serviceId,
                                             @ApiParam(value = "ScenarioRequest с текущим шагом сценария", required = true) @RequestBody ScenarioRequest request) {
        tracingHelper.addServiceCodeAndOrderId(serviceId, request);

        MainScreenService mainScreenService = mainScreenServiceRegistry.getService(request.getScenarioDto().getTargetCode());
        mainScreenService.saveCacheToDraft(serviceId, request);

        ScenarioResponse response = new ScenarioResponse();
        response.setScenarioDto(request.getScenarioDto());
        response.setIsInternalScenario(request.getIsInternalScenario());
        response.setCallBackOrderId(request.getCallBackOrderId());
        response.setCallBackServiceId(request.getCallBackServiceId());
        response.setHealth(healthHolder.get());
        return response;
    }

    /**
     * Method for calling delirium with action and performing next scenario step.
     */
    @Override
    @PostMapping(value = "/deliriumNextStep")
    public ScenarioResponse deliriumActionAndNextStep(@PathVariable String serviceId, @RequestBody DeliriumActionRequest request) {
        tracingHelper.addServiceCodeAndOrderId(serviceId, request.getScenarioDto());
        MainScreenService mainScreenService = mainScreenServiceRegistry.getService(request.getScenarioDto().getTargetCode());
        mainScreenService.setStatusId(request.getScenarioDto());
        ScenarioRequest scenarioRequest = new ScenarioRequest();
        scenarioRequest.setScenarioDto(request.getScenarioDto());
        ScenarioResponse result = mainScreenService.getNextScreen(scenarioRequest, serviceId);
        deliriumService.requestAction(result.getScenarioDto(), request.getDeliriumAction());
        result.setHealth(healthHolder.get());
        return result;
    }

    /**
     * Method for for submit button handle
     */
    @PostMapping(value = "/skipStep")
    public ScenarioResponse skipStep(@PathVariable String serviceId, @RequestBody ScenarioRequest request) {
        tracingHelper.addServiceCodeAndOrderId(serviceId, request);
        MainScreenService mainScreenService = mainScreenServiceRegistry.getService(request.getScenarioDto().getTargetCode());
        mainScreenService.setStatusId(request.getScenarioDto());
        ScenarioResponse result = mainScreenService.skipStep(request, serviceId);
        result.setHealth(healthHolder.get());
        return result;
    }

    /**
     * Method for for submit button handle
     */
    @Override
    @ApiOperation(value = "Получение предыдущего шага в сценарии услуги")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Неверные параметры"),
            @ApiResponse(code = 401, message = "Не авторизованный пользователь"),
            @ApiResponse(code = 403, message = "Нет прав"),
            @ApiResponse(code = 500, message = "Внутренняя ошибка")})
    @PostMapping(value = "/getPrevStep")
    public ScenarioResponse getPrevStep(@ApiParam(value = "Id услуги", required = true) @PathVariable String serviceId,
                                        @ApiParam(value = "ScenarioRequest с текущим шагом сценария", required = true) @RequestBody ScenarioRequest request,
                                        @ApiParam(value = "Количество шагов на которое надо вернуться назад", defaultValue = "1") @RequestParam(defaultValue = "1") Integer stepsBack) {
        tracingHelper.addServiceCodeAndOrderId(serviceId, request);
        MainScreenService mainScreenService = mainScreenServiceRegistry.getService(request.getScenarioDto().getTargetCode());
        mainScreenService.setStatusId(request.getScenarioDto());
        ScenarioResponse result = mainScreenService.getPrevScreen(request, serviceId, stepsBack);
        result.setHealth(healthHolder.get());
        return result;
    }

    /**
     * Method for checking if user has already orderId and draft for a service
     * This method also check if it's an invitation scenario case
     * If orderId for user exists than check if current user is taking part as soapplicant
     * If current user's oid is found in participants than ScenarioResponse is returned with special flag (inviteCase=true)
     *
     * @param serviceId      service ID
     * @param initServiceDto service init parameters
     * @return scenario response
     */
    @Override
    @PostMapping(value = "checkIfOrderIdExists")
    public OrderListInfoDto checkIfOrderIdExists(@PathVariable String serviceId, @RequestBody InitServiceDto initServiceDto) {
        tracingHelper.addServiceCodeAndOrderId(serviceId, initServiceDto);
        MainScreenService mainScreenService = mainScreenServiceRegistry.getService(initServiceDto.getTargetId());
        return mainScreenService.getOrderInfo(initServiceDto, serviceId);
    }

    @PostMapping(value = "getOrderStatus")
    public OrderListInfoDto getOrderStatusById(@PathVariable String serviceId, @RequestBody InitServiceDto initServiceDto) {
        tracingHelper.addServiceCodeAndOrderId(serviceId, initServiceDto);
        MainScreenService mainScreenService = mainScreenServiceRegistry.getService(initServiceDto.getTargetId());
        return mainScreenService.getOrderInfoById(initServiceDto, serviceId);
    }

    /**
     * Метод используется при переходе по прямой ссылке к нам на портал.
     */
    @PostMapping(value = "external")
    public ScenarioResponse fromExternal(@RequestBody ExternalServiceRequest externalServiceRequest){
        ScenarioFromExternal scenarioFromExternal = ScenarioFromExternal
                .builder()
                .externalApplicantAnswers(externalServiceRequest.getAnswers())
                .targetId(externalServiceRequest.getTargetId())
                .screenId(externalServiceRequest.getScreenId())
                .serviceId(externalServiceRequest.getServiceId())
                .build();
        MainScreenService mainScreenService = mainScreenServiceRegistry.getService(externalServiceRequest.getTargetId());
        return mainScreenService.prepareScenarioFromExternal(scenarioFromExternal);
    }

}
