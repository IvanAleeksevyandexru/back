package ru.gosuslugi.pgu.fs.component.confirm

import org.springframework.http.HttpStatus
import ru.atc.carcass.security.rest.model.EsiaContact
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.components.ComponentAttributes
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.DisplayRequest
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.LinkedValue
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.common.component.validation.NotBlankValidation
import ru.gosuslugi.pgu.fs.common.exception.ErrorModalException
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalDescriptorService
import ru.gosuslugi.pgu.fs.service.ConfirmEmailService
import spock.lang.Specification

class ConfirmEmailCodeInputComponentTest extends Specification {
    public static final String ROUTING_CODE = "123"
    public static final String TOO_MANY_REQUESTS_EMAIL_EXCEPTION_MESSAGE = "Возможность подать заявление будет заблокирована на 60 дней, если количество запросов кода превысит 6 раз. Получить код повторно можно через 15 минут"
    public static final String EMAIL = "test@txt.ru"
    public static final String SMS_CONFORMATION_SERVICE_ERROR = "Ошибка обращения к сервису"
    public static final String CONFIRMATION_CODE = "9876"
    public static final String WRONG_CODE = "Неправильный код"
    public static final String TOO_MANY_TRYES = "Превышено количество попыток ввода кода"
    public static final String CODE_TIMEOUT = "Истек срок действия кода"
    ConfirmEmailCodeInputComponent component
    ConfirmEmailService confirmEmailService
    UserPersonalData userPersonalData
    ErrorModalDescriptorService errorModalDescriptorService
    Map<String, String> incorrectAnswers
    Map.Entry<String, ApplicantAnswer> entry


    void setup() {
        confirmEmailService = Stub(ConfirmEmailService)
        userPersonalData = Stub(UserPersonalData)
        errorModalDescriptorService = Stub(ErrorModalDescriptorService)
        component = new ConfirmEmailCodeInputComponent(confirmEmailService, userPersonalData, errorModalDescriptorService)
        incorrectAnswers = new HashMap<>()
        entry = new MapEntry("value", new ApplicantAnswer(true, CONFIRMATION_CODE))
    }

    def "данные есть в кэше"() {
        given:
        FieldComponent fieldComponent = createFieldComponent()
        ScenarioDto scenario = createSimpleScenario()
        scenario.getCachedAnswers().put("internalProcessSuccess", new ApplicantAnswer(true, ""))

        when:
        ComponentResponse result = component.getInitialValue(fieldComponent, scenario)

        then:
        result == ComponentResponse.empty()
    }

    def "данных нет в кэше но  есть в ответах"() {
        given:
        FieldComponent fieldComponent = createFieldComponent()
        ScenarioDto scenario = createSimpleScenario()
        scenario.getApplicantAnswers().put("internalProcessSuccess", new ApplicantAnswer(true, ""))

        when:
        ComponentResponse result = component.getInitialValue(fieldComponent, scenario)

        then:
        result == ComponentResponse.empty()
    }

    def "данных нет в кэше и в ответах(happy path)"() {
        given:
        FieldComponent fieldComponent = createFieldComponent()
        ScenarioDto scenario = createSimpleScenario()
        scenario.getServiceInfo().setRoutingCode(ROUTING_CODE)

        when:
        confirmEmailService.sendConfirmationEmail(_) >> HttpStatus.OK
        userPersonalData.getContacts() >> [new EsiaContact(type: EsiaContact.Type.EMAIL.getCode(), vrfStu: ComponentAttributes.VERIFIED_ATTR, value: EMAIL)]
        ComponentResponse result = component.getInitialValue(fieldComponent, scenario)

        then:
        fieldComponent.getAttrs()["email"] == EMAIL
        result == ComponentResponse.empty()
    }


    def "данных нет в кэше и в ответах(HttpStatus==TOO_MANY_REQUESTS)"() {
        given:
        FieldComponent fieldComponent = createFieldComponent()
        ScenarioDto scenario = createSimpleScenario()
        scenario.getServiceInfo().setRoutingCode(ROUTING_CODE)

        when:
        confirmEmailService.sendConfirmationEmail(_) >> HttpStatus.TOO_MANY_REQUESTS
        ComponentResponse result = component.getInitialValue(fieldComponent, scenario)

        then:
        def e = thrown(ErrorModalException)
        e.message == TOO_MANY_REQUESTS_EMAIL_EXCEPTION_MESSAGE
    }

    def "данных нет в кэше и в ответах(HttpStatus !=TOO_MANY_REQUESTS, HttpStatus != OK)"() {
        given:
        FieldComponent fieldComponent = createFieldComponent()

        ScenarioDto scenario = createSimpleScenario()
        scenario.getServiceInfo().setRoutingCode(ROUTING_CODE)

        when:
        confirmEmailService.sendConfirmationEmail(_) >>  HttpStatus.SERVICE_UNAVAILABLE
        ComponentResponse result = component.getInitialValue(fieldComponent, scenario)

        then:
        def e = thrown(FormBaseWorkflowException)
        e.message == SMS_CONFORMATION_SERVICE_ERROR
    }

    static ScenarioDto createSimpleScenario() {
        new ScenarioDto(
                orderId: 1,
                display: new DisplayRequest(components: []),
        )
    }

    static FieldComponent createFieldComponent() {
        new FieldComponent(id: "internalProcessSuccess",
                type: "ConfirmEmailCodeInput",
                label: "код подтверждения",
                attrs: [
                    codeLength: 4,
                    characterMask: "\\d",
                    resendCodeUrl: "service/actions/resendConfirmationCode"
                ],
                linkedValues: [
                        new LinkedValue(
                                argument: "email",
                                source: "pd1"
                        )
                ])
    }

    def "getValidations возвращает непустой лист с одним NotBlankValidation"() {
        expect:
        with(component.getValidations()) {
            size() == 1
            get(0) instanceof NotBlankValidation
        }
    }

    def "OK от validateAfterSubmit"() {
        given:
        FieldComponent fieldComponent = createFieldComponent()

        ScenarioDto scenario = createSimpleScenario()
        scenario.getServiceInfo().setRoutingCode(ROUTING_CODE)

        when:
        confirmEmailService.checkConfirmationEmail(_ as String,_ as Long) >> HttpStatus.OK
        component.validateAfterSubmit(incorrectAnswers, entry, scenario, fieldComponent)

        then:
        incorrectAnswers.size() == 0
    }

    def "должен добавить в incorrectAnswers правильную ошибку: #errorMessage"() {
        given:
        FieldComponent fieldComponent = createFieldComponent()

        ScenarioDto scenario = createSimpleScenario()
        scenario.getServiceInfo().setRoutingCode(ROUTING_CODE)

        when:
        confirmEmailService.checkConfirmationEmail(_ as String,_ as Long) >> httpStatus
        component.validateAfterSubmit(incorrectAnswers, entry, scenario, fieldComponent)

        then:
        incorrectAnswers.size() == 1
        incorrectAnswers.get("value") ==  errorMessage

        where:
        httpStatus << [HttpStatus.CONFLICT, HttpStatus.TOO_MANY_REQUESTS, HttpStatus.REQUEST_TIMEOUT, HttpStatus.SERVICE_UNAVAILABLE]
        errorMessage << [WRONG_CODE, TOO_MANY_TRYES, CODE_TIMEOUT, SMS_CONFORMATION_SERVICE_ERROR]
    }

}
