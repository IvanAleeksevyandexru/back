package ru.gosuslugi.pgu.fs.component.confirm

import ru.atc.carcass.security.rest.model.EsiaContact
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import spock.lang.Specification

import static ru.gosuslugi.pgu.components.ComponentAttributes.VERIFIED_ATTR

class ConfirmPersonalUserEmailComponentTest extends Specification {

    private static FieldComponent COMPONENT
    private static ScenarioDto SCENARIO_DTO
    private static ConfirmPersonalUserEmailComponent USER_EMAIL_COMPONENT
    private static ConfirmPersonalUserEmailComponent USER_EMAIL_COMPONENT_WITH_DATA
    private static UserPersonalData userPersonalDataWithoutData
    private static UserPersonalData userPersonalDataWithData
    private static UserOrgData userOrgData
    private static final String CORRECT_EMAIL = 'test@test.ru'
    private static final String INCORRECT_EMAIL = 'testAtest.ru'
    static EsiaContact contact
    private static final String KEY = "key"
    private static final String SUFFIX = "-component.json"

    def setupSpec() {
        SCENARIO_DTO = new ScenarioDto()
        userPersonalDataWithoutData = Stub(UserPersonalData)
        userOrgData = new UserOrgData()
        USER_EMAIL_COMPONENT = new ConfirmPersonalUserEmailComponent(userPersonalDataWithoutData, userOrgData)
        COMPONENT = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), SUFFIX), FieldComponent)
        userPersonalDataWithData = new UserPersonalData()
        contact = new EsiaContact()
        contact.setValue(CORRECT_EMAIL)
        contact.setType(EsiaContact.Type.EMAIL.getCode())
        contact.setVrfStu(VERIFIED_ATTR)
        userPersonalDataWithData.setContacts(List.of(contact))
        USER_EMAIL_COMPONENT_WITH_DATA = new ConfirmPersonalUserEmailComponent(userPersonalDataWithData, userOrgData)

    }

    def 'Get type'() {
        expect:
        USER_EMAIL_COMPONENT.getType() == ComponentType.ConfirmPersonalUserEmail
    }

    def 'Test getInitialValue: get initial value response is empty'() {
        expect:
        USER_EMAIL_COMPONENT.getInitialValue(COMPONENT, SCENARIO_DTO) == ComponentResponse.empty()
    }

    def 'Test getInitialValue: get initial value response is not empty'() {
        expect:
        USER_EMAIL_COMPONENT_WITH_DATA.getInitialValue(COMPONENT, SCENARIO_DTO).get() == CORRECT_EMAIL
    }

    def 'Test preValidate: get empty error from empty defaultHint'() {
        given:
        def initialValue = USER_EMAIL_COMPONENT.getInitialValue(COMPONENT, SCENARIO_DTO)
        USER_EMAIL_COMPONENT.preValidate(initialValue, COMPONENT, SCENARIO_DTO)
        expect:
        COMPONENT.getErrors() == null
    }

    def 'Test preValidate: get error from defaultHint'() {
        given:
        def initialValue = USER_EMAIL_COMPONENT.getInitialValue(COMPONENT, SCENARIO_DTO)
        def component =
                JsonProcessingUtil.fromJson(
                        JsonFileUtil.getJsonFromFile(this.getClass(), "-withDefaultHint" + SUFFIX),
                        FieldComponent
                )
        USER_EMAIL_COMPONENT.preValidate(initialValue, component, SCENARIO_DTO)
        expect:
        component.getErrors().size() == 1
    }

    def 'Test preValidate: component with correct email'() {
        given:
        def initialValue = USER_EMAIL_COMPONENT_WITH_DATA.getInitialValue(COMPONENT, SCENARIO_DTO)
        USER_EMAIL_COMPONENT_WITH_DATA.preValidate(initialValue, COMPONENT, SCENARIO_DTO)
        expect:
        COMPONENT.getErrors() == null
    }

    def 'Test preValidate: component with incorrect email'() {
        given:
        def userPersonalDataWithIncorrectData = new UserPersonalData()
        contact = new EsiaContact()
        contact.setValue(INCORRECT_EMAIL)
        contact.setType(EsiaContact.Type.EMAIL.getCode())
        contact.setVrfStu(VERIFIED_ATTR)
        userPersonalDataWithIncorrectData.setContacts(List.of(contact))

        def confirmPersonalUserEmailComponent =
                new ConfirmPersonalUserEmailComponent(userPersonalDataWithIncorrectData, userOrgData)
        def initialValue =
                confirmPersonalUserEmailComponent.getInitialValue(COMPONENT, SCENARIO_DTO)
        def component =
                JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), SUFFIX), FieldComponent)

        confirmPersonalUserEmailComponent.preValidate(initialValue, component, SCENARIO_DTO)

        def errors = component.getErrors()
        def error = errors.get(0)
        expect:
        errors.size() == 1
        error.getDesc() == ConfirmPersonalUserEmailComponent.DEFAULT_EMAIL_ERROR_DESC
        error.getTitle() == ConfirmPersonalUserEmailComponent.DEFAULT_EMAIL_ERROR_MSG
    }

    def 'Test getRegExpValidationAttrs: enableCustomValidation is true'() {
        given:
        def component =
                JsonProcessingUtil.fromJson(
                        JsonFileUtil.getJsonFromFile(this.getClass(), "-enableCustomValidation" + SUFFIX),
                        FieldComponent
                )
        expect:
        USER_EMAIL_COMPONENT
                .getRegExpValidationAttrs(component) == List.of(
                Map.of("type", "RegExp",
                        "value", "^[0-9а-яА-Яa-zA-Z_.-]{2,30}[@]{1}[0-9а-яА-Яa-zA-Z_.-]{2,30}[.]{1}[а-яА-Яa-zA-Z]{2,5}\$",
                        "errorMsg", "Поле должно быть заполнено"
                )
        )
    }

    def 'Test validateAfterSubmit: empty email'() {
        given:
        def entry = Map.entry(KEY, new ApplicantAnswer(false, ""))
        def incorrectAnswers = new HashMap<String, String>()
        USER_EMAIL_COMPONENT.validateAfterSubmit(incorrectAnswers, entry, SCENARIO_DTO, COMPONENT)
        expect:
        incorrectAnswers.get(KEY) == ConfirmPersonalUserEmailComponent.EMPTY_EMAIL_WARRING_MSG
    }

    def 'Test validateAfterSubmit: incorrect email'() {
        given:
        def entry = Map.entry(KEY, new ApplicantAnswer(false, INCORRECT_EMAIL))
        def incorrectAnswers = new HashMap<String, String>()
        USER_EMAIL_COMPONENT.validateAfterSubmit(incorrectAnswers, entry, SCENARIO_DTO, COMPONENT)
        expect:
        incorrectAnswers.get(KEY) == ConfirmPersonalUserEmailComponent.DEFAULT_EMAIL_ERROR_MSG
    }

    def 'Test validateAfterSubmit: correct email with data'() {
        given:
        def entry = Map.entry(KEY, new ApplicantAnswer(false, CORRECT_EMAIL))
        def incorrectAnswers = new HashMap<String, String>()
        USER_EMAIL_COMPONENT_WITH_DATA.validateAfterSubmit(incorrectAnswers, entry, SCENARIO_DTO, COMPONENT)
        expect:
        incorrectAnswers.isEmpty()
    }

    def 'Test validateAfterSubmit: correct email without data'() {
        given:
        def entry = Map.entry(KEY, new ApplicantAnswer(false, CORRECT_EMAIL))
        def incorrectAnswers = new HashMap<String, String>()
        USER_EMAIL_COMPONENT.validateAfterSubmit(incorrectAnswers, entry, SCENARIO_DTO, COMPONENT)
        expect:
        incorrectAnswers.get(KEY) == ConfirmPersonalUserEmailComponent.NOT_VERIFIED_EMAIL_ERROR_MSG
    }
}
