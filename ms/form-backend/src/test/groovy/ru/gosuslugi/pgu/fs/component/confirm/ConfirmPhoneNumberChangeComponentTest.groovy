package ru.gosuslugi.pgu.fs.component.confirm

import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.validation.NotBlankValidation
import ru.gosuslugi.pgu.fs.common.component.validation.RegExpValidation
import ru.gosuslugi.pgu.fs.esia.dto.EsiaContactState
import ru.gosuslugi.pgu.fs.service.PersonContactService
import spock.lang.Specification

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ConfirmPhoneNumberChangeComponentTest extends Specification {

    private static ConfirmPhoneNumberChangeComponent component

    private static final String WARNING_KEY = "warning_key"
    private static final String PHONE_NUMBER = "99999999999"
    private static final String PREPARED_PHONE_NUMBER_FOR_ESIA = "99999999991"
    private static final String WRONG_VERIFIED_PHONE_NUMBER = "99999999992"

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private static final String WARNING_PHONE_USED_IN_LAST_30_DAYS = "Номер мобильного телефона уже используется в другой учетной записи. Если вы уже регистрировались, попробуйте войти в свою учетную запись, либо укажите другой номер мобильного телефона."
    private static final String WARNING_PHONE_USED_FOR_CURRENT_PROFILE = "Номер мобильного телефона уже используется в учетной записи."

    def setupSpec() {
        component = new ConfirmPhoneNumberChangeComponent(Mock(PersonContactService), Mock(UserPersonalData))
    }

    def 'Get type'() {
        expect:
        component.getType() == ComponentType.PhoneNumberChangeInput
    }

    def 'Test getValidations'() {
        when:
        def result = component.getValidations()
        then:
        assert result.size() == 2
        assert result.stream().filter({ v -> (v instanceof NotBlankValidation) }).any()
        assert result.stream().filter({ v -> (v instanceof RegExpValidation) }).any()
    }

    def 'Test validateAfterSubmit WARNING_PHONE_USED_FOR_CURRENT_PROFILE'() {
        given:
        PersonContactService personContactService = Mock(PersonContactService) {
            preparePhoneNumberForEsia(PHONE_NUMBER) >> PREPARED_PHONE_NUMBER_FOR_ESIA
        }

        UserPersonalData userPersonalData = Mock(UserPersonalData) {
            getVerifiedPhoneNumber() >> PREPARED_PHONE_NUMBER_FOR_ESIA
        }

        ConfirmPhoneNumberChangeComponent componentForValidate = new ConfirmPhoneNumberChangeComponent(personContactService, userPersonalData)

        Map<String, String> incorrectAnswers = new HashMap<>()

        when:
        componentForValidate.validateAfterSubmit(incorrectAnswers, WARNING_KEY, PHONE_NUMBER)

        then:
        assert incorrectAnswers == Map.of(WARNING_KEY, WARNING_PHONE_USED_FOR_CURRENT_PROFILE)
    }

    def 'Test validateAfterSubmit WARNING_PHONE_USED_IN_LAST_30_DAYS'() {
        given:
        PersonContactService personContactService = Mock(PersonContactService) {
            preparePhoneNumberForEsia(PHONE_NUMBER) >> PREPARED_PHONE_NUMBER_FOR_ESIA
            validatePhoneNumber(PREPARED_PHONE_NUMBER_FOR_ESIA) >>
                    new EsiaContactState(
                            'state': true,
                            'verifiedOn': DATE_FORMATTER.format(LocalDate.now() - 1)
                    )
        }

        UserPersonalData userPersonalData = Mock(UserPersonalData) {
            getVerifiedPhoneNumber() >> WRONG_VERIFIED_PHONE_NUMBER
        }

        ConfirmPhoneNumberChangeComponent componentForValidate =
                new ConfirmPhoneNumberChangeComponent(personContactService, userPersonalData)

        Map<String, String> incorrectAnswers = new HashMap<>()

        when:
        componentForValidate.validateAfterSubmit(incorrectAnswers, WARNING_KEY, PHONE_NUMBER)

        then:
        assert incorrectAnswers == Map.of(WARNING_KEY, WARNING_PHONE_USED_IN_LAST_30_DAYS)
    }

    def 'Test postProcess'() {
        given:
        PersonContactService personContactService = Mock(PersonContactService) {
            preparePhoneNumberForEsia(PHONE_NUMBER) >> PREPARED_PHONE_NUMBER_FOR_ESIA
            updatePhoneNumber(PREPARED_PHONE_NUMBER_FOR_ESIA, Mock(ScenarioDto)) >> true
        }
        ConfirmPhoneNumberChangeComponent componentForValidate =
                new ConfirmPhoneNumberChangeComponent(personContactService, Mock(UserPersonalData))
        when:
        componentForValidate.postProcess(Mock(FieldComponent), Mock(ScenarioDto), PHONE_NUMBER)
        then:
        1 * personContactService.preparePhoneNumberForEsia(_)
        1 * personContactService.updatePhoneNumber(_, _)
    }
}
