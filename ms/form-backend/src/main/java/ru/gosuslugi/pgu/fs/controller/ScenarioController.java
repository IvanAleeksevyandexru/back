package ru.gosuslugi.pgu.fs.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.gosuslugi.pgu.common.core.service.HealthHolder;
import ru.gosuslugi.pgu.dto.DeliriumActionRequest;
import ru.gosuslugi.pgu.dto.ExternalServiceRequest;
import ru.gosuslugi.pgu.dto.InitServiceDto;
import ru.gosuslugi.pgu.dto.ScenarioFromExternal;
import ru.gosuslugi.pgu.dto.ScenarioRequest;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.dto.order.OrderListInfoDto;
import ru.gosuslugi.pgu.fs.service.DeliriumService;
import ru.gosuslugi.pgu.fs.service.MainScreenService;
import ru.gosuslugi.pgu.fs.service.custom.MainScreenServiceRegistry;
import ru.gosuslugi.pgu.fs.utils.TracingHelper;

import java.util.Objects;
import java.util.Optional;

/**
 * Controller that handle all methods for scenarios
 * Next/prev pages
 * Расширение для https://jira.egovdev.ru/browse/EPGUCORE-90939 - Закрытие услуги кукой через JSON
 */
@RestController
@RequestMapping(value = "service/{serviceId}/scenario", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ScenarioController {

    private final DeliriumService deliriumService;
    private final HealthHolder healthHolder;
    private final TracingHelper tracingHelper;
    private final MainScreenServiceRegistry mainScreenServiceRegistry;

    /**
     * Method for getting first screen and  generating new orderId
     *
     * @return scenario response
     */
    @Operation(summary = "Инициализация услуги")
    @PostMapping(value = "/getService")
    public ScenarioResponse getServiceInitScreen(@Parameter(description = "Id услуги", required = true) @PathVariable String serviceId,
                                                 @Parameter(description = "Дополнительная информация для инициализации услуги", required = true) @RequestBody InitServiceDto initServiceDto) {
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
    @Operation(summary = "Получение следующего шага в сценарии услуги")
    @PostMapping(value = "/getNextStep", produces = "application/json; charset=utf-8")
    public ScenarioResponse getNextStep(@Parameter(description = "Id услуги", required = true) @PathVariable String serviceId,
                                        @Parameter(description = "ScenarioRequest с текущим шагом сценария", required = true) @RequestBody ScenarioRequest request) {
        tracingHelper.addServiceCodeAndOrderId(serviceId, request);
        MainScreenService mainScreenService = mainScreenServiceRegistry.getService(request.getScenarioDto().getTargetCode());
        mainScreenService.setStatusId(request.getScenarioDto());
        ScenarioResponse result = mainScreenService.getNextScreen(request, serviceId);
        result.setHealth(healthHolder.get());
        return result;
    }

    @Operation(summary = "Сохранение кешированных значений заявления в черновик")
    @PostMapping(value = "/saveCacheToDraft", produces = "application/json; charset=utf-8")
    public ScenarioResponse saveCacheToDraft(@Parameter(description = "Id услуги", required = true) @PathVariable String serviceId,
                                             @Parameter(description = "ScenarioRequest с текущим шагом сценария", required = true) @RequestBody ScenarioRequest request) {
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
    @Operation(summary = "Переход на следующий шаг и выполнение действия в delirium")
    @PostMapping(value = "/deliriumNextStep")
    public ScenarioResponse deliriumActionAndNextStep(
            @Parameter(description = "Идентификатор услуги") @PathVariable String serviceId,
            @RequestBody DeliriumActionRequest request) {
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
    @Operation(summary = "Пропуск текущего шага", description =
            "Пропуск шага используется в сценариях отсутствия некоторых необязательных данных\n\n" +
                    "Например: при отсутствии адреса временной регистрации проживания или при отсутствии электронной подписи"
    )
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
    @Operation(summary = "Получение предыдущего шага в сценарии услуги")
    @PostMapping(value = "/getPrevStep")
    public ScenarioResponse getPrevStep(@Parameter(description = "Id услуги", required = true) @PathVariable String serviceId,
                                        @Parameter(description = "ScenarioRequest с текущим шагом сценария", required = true) @RequestBody ScenarioRequest request,
                                        @Parameter(description = "Количество шагов на которое надо вернуться назад") @RequestParam(required = false) Integer stepsBack,
                                        @Parameter(description = "Id экрана на который надо вернуться") @RequestParam(required = false) String screenId
    ) {
        tracingHelper.addServiceCodeAndOrderId(serviceId, request);
        MainScreenService mainScreenService = mainScreenServiceRegistry.getService(request.getScenarioDto().getTargetCode());
        mainScreenService.setStatusId(request.getScenarioDto());
        ScenarioResponse result = mainScreenService.getPrevScreen(request, serviceId, stepsBack, screenId);
        result.setHealth(healthHolder.get());
        return result;
    }

    /**
     * Method for checking if user has already orderId and draft for a service
     * This method also check if it's an invitation scenario case
     * If orderId for user exists than check if current user is taking part as co-applicant
     * If current user's oid is found in participants than ScenarioResponse is returned with special flag (inviteCase=true)
     *
     * @param serviceId      service ID
     * @param initServiceDto service init parameters
     * @return scenario response
     */
    @Operation(summary = "Получение информации о наличии заявлений для текущего черновика")
    @PostMapping(value = "checkIfOrderIdExists")
    public OrderListInfoDto checkIfOrderIdExists(
            @Parameter(description = "Id услуги", required = true) @PathVariable String serviceId,
            @RequestBody InitServiceDto initServiceDto,
            @CookieValue(value = "newSF") Optional<Object> newSFCookie
    ) {
        tracingHelper.addServiceCodeAndOrderId(serviceId, initServiceDto);
        MainScreenService mainScreenService = mainScreenServiceRegistry.getService(initServiceDto.getTargetId());
        // требуется ли выполнить редирект
        boolean isRedirect = shouldNewSFRedirect(newSFCookie, mainScreenService, serviceId);
        var orderInfo = mainScreenService.getOrderInfo(initServiceDto, serviceId);
        orderInfo.setClosedByCookie(isRedirect);
        return orderInfo;
    }

    @Operation(summary = "Получение информации по идентификатору заявления", description =
            "Идентификатор заявления ожидается в теле запроса (orderId)"
    )
    @PostMapping(value = "getOrderStatus")
    public OrderListInfoDto getOrderStatusById(
            @Parameter(description = "Id услуги", required = true) @PathVariable String serviceId,
            @RequestBody InitServiceDto initServiceDto
    ) {
        tracingHelper.addServiceCodeAndOrderId(serviceId, initServiceDto);
        MainScreenService mainScreenService = mainScreenServiceRegistry.getService(initServiceDto.getTargetId());
        return mainScreenService.getOrderInfoById(initServiceDto, serviceId);
    }

    /**
     * Метод используется при переходе по прямой ссылке к нам на портал.
     */
    @Operation(summary = "Инициализации услуги в случае перехода по прямой ссылке на портал")
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

    /**
     * Требуется ли выполнить редирект
     */
    private boolean shouldNewSFRedirect(Optional<Object> newSFCookie, MainScreenService mainScreenService, String serviceId) {

        boolean closedByNewSFСookie = false;
        Optional<Boolean> optClosedByNewSFСookie = Optional.ofNullable(mainScreenService.getServiceDescriptor(serviceId).getClosedByNewSFСookie());
        if (optClosedByNewSFСookie.isPresent()) {
            closedByNewSFСookie = optClosedByNewSFСookie.get();
        }

        if (closedByNewSFСookie) {
            boolean newSFLabel = false;
            if (newSFCookie.isPresent()) {
                Object sfValue = newSFCookie.get();
                newSFLabel = Boolean.valueOf(String.valueOf(sfValue));
            }
            return !newSFLabel;
        } else {
            return false;
        }
    }
}
