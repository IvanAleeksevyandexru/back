package ru.gosuslugi.pgu.fs.component.confirm


import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.common.component.validation.NotBlankValidation
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException
import ru.gosuslugi.pgu.fs.esia.dto.EsiaContactState
import ru.gosuslugi.pgu.fs.service.PersonContactService
import spock.lang.Specification


class ConfirmPhoneNumberComponentTest extends Specification {

    private static ConfirmPhoneNumberComponent component

    @SuppressWarnings("GroovyAccessibility")
    private static PHONE_NUMBER_ARGUMENT_NAME = ConfirmPhoneNumberComponent.PHONE_NUMBER_ARGUMENT_NAME

    def setupSpec() {
        component = new ConfirmPhoneNumberComponent(Mock(PersonContactService))
    }

    def 'component type'() {
        expect:
        component.getType() == ComponentType.PhoneNumberConfirmCodeInput
    }

    def 'get validations'() {
        when:
        def result = component.getValidations()

        then:
        result.size() == 1
        result.stream().filter({ v -> (v instanceof NotBlankValidation) }).any()
    }

    @SuppressWarnings("GroovyAccessibility")
    def 'validate after submit'() {
        given:
        def fieldComponent = new FieldComponent()
        def scenarioDto = Mock(ScenarioDto)
        def entry = new MapEntry("confirmationCode", new ApplicantAnswer(true, _ as String))
        def personContactService = Mock(PersonContactService) {
            checkConfirmationCode(_ as String, scenarioDto) >> confirmationCodeResult
        }
        def componentForValidate = new ConfirmPhoneNumberComponent(personContactService)
        Map<String, String> incorrectAnswers = new HashMap<>()

        when:
        componentForValidate.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent)

        then:
        incorrectAnswers.size() == incorrectAnswersCount
        incorrectAnswers.getOrDefault("confirmationCode", null) == errorMessage

        where:
        confirmationCodeResult | incorrectAnswersCount | errorMessage
        Boolean.TRUE           | 0                     | null
        Boolean.FALSE          | 1                     | ConfirmPhoneNumberComponent.ERROR_CONFIRM_CODE_MESSAGE
    }

    def 'thrown exception when argument phoneNumber is empty'() {
        given:
        def fieldComponent = Mock(FieldComponent)
        fieldComponent.getArgument(PHONE_NUMBER_ARGUMENT_NAME) >> null
        def componentForValidate = new ConfirmPhoneNumberComponent(Mock(PersonContactService))

        when:
        componentForValidate.preValidate(_ as ComponentResponse<String>, fieldComponent, Mock(ScenarioDto))

        then:
        thrown FormBaseWorkflowException
    }

    @SuppressWarnings("GroovyAccessibility")
    def 'when argument phoneNumber is not empty'() {
        given:
        PersonContactService personContactService = Mock(PersonContactService) {
            preparePhoneNumberForEsia(_ as String) >> '1234567'
            validatePhoneNumber(_ as String) >> new EsiaContactState('state': esiaState)
        }
        def componentForValidate = new ConfirmPhoneNumberComponent(personContactService)

        def fieldComponent = new FieldComponent()
        fieldComponent.getArguments().put(PHONE_NUMBER_ARGUMENT_NAME, '123-45-67')

        when:
        componentForValidate.preValidate(_ as ComponentResponse<String>, fieldComponent, Mock(ScenarioDto))

        then:
        getValidateMessage(fieldComponent) == errorMessage

        where:
        esiaState     | errorMessage
        Boolean.TRUE  | ConfirmPhoneNumberComponent.WARNING_USED_PHONE_NUMBER
        Boolean.FALSE | null
    }

    private static String getValidateMessage(FieldComponent component) {
        Objects.isNull(component.getAttrs())
                ? null
                : component.getAttrs().getOrDefault("validateMessage", null)
    }
}
