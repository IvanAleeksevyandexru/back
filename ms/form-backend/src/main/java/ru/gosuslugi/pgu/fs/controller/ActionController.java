package ru.gosuslugi.pgu.fs.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.gosuslugi.pgu.dto.ExternalOrderRequest;
import ru.gosuslugi.pgu.dto.InitServiceDto;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.ScenarioRequest;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.service.AutofillOrderService;
import ru.gosuslugi.pgu.fs.service.ChecksumService;
import ru.gosuslugi.pgu.fs.service.ConfirmCodeService;
import ru.gosuslugi.pgu.fs.service.ExternalServiceOrchestrator;
import ru.gosuslugi.pgu.fs.service.OrgContactService;
import ru.gosuslugi.pgu.fs.service.PersonContactService;
import ru.gosuslugi.pgu.fs.service.SubScreenService;
import ru.gosuslugi.pgu.fs.utils.TracingHelper;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

/**
 * Controller that handle actions
 */
@RequestMapping(value = "service/actions", produces = "application/json")
@RestController
@RequiredArgsConstructor
public class ActionController {

    private static final String EDIT_PHONE_NUMBER_SERVICE_ID = "editPhoneNumber";
    private static final String EDIT_USER_EMAIL_SERVICE_ID = "editUserEmail";
    private static final String EDIT_LEGAL_EMAIL_SERVICE_ID = "editLegalEmail";
    private static final String EDIT_USER_ADDRESS_SERVICE_ACTUAL_RESIDENCE_ID = "editUserAddressActualResidence";
    private static final String EDIT_USER_ADDRESS_SERVICE_PERMANENT_REGISTRY_ID = "editUserAddressPermanentRegistry";
    private static final String EDIT_USER_OMS = "editUserPolicy";
    private static final String CONFIRM_SMS_CODE_SERVICE_ID = "confirmSmsCode";
    private static final String CONFIRM_EMAIL_CODE_SERVICE_ID = "confirmEmailCode";

    private final SubScreenService subScreenService;
    private final PersonContactService contactService;
    private final OrgContactService orgContactService;
    private final ExternalServiceOrchestrator externalServiceOrchestrator;
    private final ConfirmCodeService confirmCodeService;
    private final TracingHelper tracingHelper;
    private final AutofillOrderService autofillOrderService;
    private final ChecksumService checksumService;

    @Operation(summary = "Запрос на изменение телефона пользователя")
    @PostMapping(value = "/editPhoneNumber")
    public ScenarioResponse editPhoneNumber(@RequestBody ScenarioRequest request) {
        tracingHelper.addServiceCodeAndOrderId(request);
        return fillInternalScenarioResponse(request, EDIT_PHONE_NUMBER_SERVICE_ID);
    }

    /**
     * Добавления/изменение email-а пользователя
     * @param request запрос
     * @return внутренний сценарий
     */
    @Operation(summary = "Запрос на изменение E-Mail пользователя")
    @PostMapping(value = "/editUserEmail")
    public ScenarioResponse editUserEmail(@RequestBody ScenarioRequest request) {
        tracingHelper.addServiceCodeAndOrderId(request);
        return fillInternalScenarioResponse(request, EDIT_USER_EMAIL_SERVICE_ID);
    }

    /**
     * Добавления/изменение email-а организации
     * @param request запрос
     * @return внутренний сценарий
     */
    @Operation(summary = "Запрос на изменение E-Mail организации")
    @PostMapping(value = "/editLegalEmail")
    public ScenarioResponse editLegalEmail(@RequestBody ScenarioRequest request) {
        tracingHelper.addServiceCodeAndOrderId(request);
        return fillInternalScenarioResponse(request, EDIT_LEGAL_EMAIL_SERVICE_ID);
    }

    @Operation(summary = "Запрос на изменение адреса фактического проживания пользователя")
    @PostMapping(value = "/editUserAddress/actualResidence")
    public ScenarioResponse editUserAddressActualResidence(@RequestBody ScenarioRequest request) {
        tracingHelper.addServiceCodeAndOrderId(request);
        return fillInternalScenarioResponse(request, EDIT_USER_ADDRESS_SERVICE_ACTUAL_RESIDENCE_ID);
    }

    @Operation(summary = "Запрос на изменение адреса постоянной регистрации пользователя")
    @PostMapping(value = "/editUserAddress/permanentRegistry")
    public ScenarioResponse editUserAddressPermanentRegistry(@RequestBody ScenarioRequest request) {
        tracingHelper.addServiceCodeAndOrderId(request);
        return fillInternalScenarioResponse(request, EDIT_USER_ADDRESS_SERVICE_PERMANENT_REGISTRY_ID);
    }

    @Operation(summary = "Запрос на изменение полиса ОМС")
    @PostMapping(value = "/editUserPolicy")
    public ScenarioResponse editUserPolicy(@RequestBody ScenarioRequest request) {
        tracingHelper.addServiceCodeAndOrderId(request);
        return fillInternalScenarioResponse(request, EDIT_USER_OMS);
    }

    @Operation(summary = "Запрос на подтверждение кода СМС")
    @PostMapping(value = "/confirmSmsCode")
    public ScenarioResponse sendConfirmationCode(@RequestBody ScenarioRequest request) {
        tracingHelper.addServiceCodeAndOrderId(request);
        return fillInternalScenarioResponse(request, CONFIRM_SMS_CODE_SERVICE_ID);
    }

    @Operation(summary = "Запрос нового подтверждения по заявлению пользователя")
    @PostMapping(value = "/resendConfirmationCode")
    public void resendConfirmationCode(@RequestBody ScenarioRequest request) {
        tracingHelper.addServiceCodeAndOrderId(request);
        Long originalOrderId = Long.parseLong(request.getScenarioDto().getServiceInfo().getRoutingCode());
        confirmCodeService.sendConfirmationCode(originalOrderId);
    }

    @Operation(summary = "Запрос нового подтверждения для телефона пользователя")
    @PostMapping(value = "/resendPhoneConfirmationCode")
    public void resendPhoneConfirmCode(@RequestBody ScenarioRequest request) {
        tracingHelper.addServiceCodeAndOrderId(request);
        contactService.resendPhoneConfirmationCode(request.getScenarioDto());
    }

    @Operation(summary = "Возвращает текущие дату и время на сервере в UTC")
    @GetMapping(value = "/currentDateTime")
    public String currentDateTime() {
        return ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX"));
    }

    /**
     * Запрос нового подтверждения для email-а пользователя
     * @param request запрос
     */
    @Operation(summary = "Запрос нового подтверждения для email-а пользователя")
    @PostMapping(value = "/resendEmailConfirmation")
    public void resendEmailConfirmation(@RequestBody ScenarioRequest request) {
        tracingHelper.addServiceCodeAndOrderId(request);
        contactService.resendEmailConfirmation(request.getScenarioDto());
    }

    /**
     * Запрос нового подтверждения для email-а организации
     * @param request запрос
     */
    @Operation(summary = "Запрос нового подтверждения для email-а организации")
    @PostMapping(value = "/resendLegalEmailConfirmation")
    public void resendLegalEmailConfirmation(@RequestBody ScenarioRequest request) {
        tracingHelper.addServiceCodeAndOrderId(request);
        orgContactService.resendEmailConfirmation(request.getScenarioDto());
    }

    /**
     * Запрос во внешний сервис
     * @param component компонент с данными для запроса
     */
    @Operation(summary = "Запрос во внешний сервис")
    @PostMapping(value = "/externalIntegration")
    public String externalIntegration(@RequestBody FieldComponent component) {
        return externalServiceOrchestrator.callExternalService(component);
    }

    @Operation(summary = "Отправка кода для подтверждения E-Mail")
    @PostMapping(value = "/confirmEmailCode")
    public ScenarioResponse sendConfirmationEmailCode(@RequestBody ScenarioRequest request) {
        return fillInternalScenarioResponse(request, CONFIRM_EMAIL_CODE_SERVICE_ID);
    }

    /**
     * Автоматическая генерация заявления и отправка его в ведомство
     * @param request запрос
     * @return сгенерированный {@link ScenarioDto}
     */
    @Operation(summary = "Автоматическая генерация заявления и отправка его в ведомство")
    @PostMapping(value = "/autofillOrder")
    public ScenarioDto autofillOrder(@RequestBody ExternalOrderRequest request) {
        return autofillOrderService.processExternalOrderRequest(request);
    }

    /**
     * Генегация чексуммы с указанными параметрами + uid
     * @param params параметры для вычислений чексуммы
     * @return сгенерированная чексумма
     */
    @Operation(summary = "Возвращает сгенерированную чексумму")
    @GetMapping(value = "/checksum")
    public String checksum(@RequestParam Map<String,String> params) {
        return checksumService.computeChecksum(params);
    }

    private ScenarioResponse fillInternalScenarioResponse(ScenarioRequest request, String internalServiceId) {
        ScenarioDto scenarioRequest = request.getScenarioDto();
        InitServiceDto serviceInfo = new InitServiceDto();
        serviceInfo.getServiceInfo().setRoutingCode(Objects.toString(scenarioRequest.getOrderId()));
        ScenarioResponse responseBox = subScreenService.getInitScreen(internalServiceId, serviceInfo, scenarioRequest.getServiceCode());
        ScenarioDto scenarioResponse = responseBox.getScenarioDto();
        responseBox.setCallBackOrderId(scenarioRequest.getOrderId());
        responseBox.setCallBackServiceId(scenarioRequest.getServiceId());
        responseBox.setIsInternalScenario(true);
        scenarioResponse.setServiceId(internalServiceId);
        scenarioResponse.setOrderId(scenarioRequest.getOrderId());
        return responseBox;
    }
}
