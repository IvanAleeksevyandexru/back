package ru.gosuslugi.pgu.fs.component.userdata

import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.PersonWithAge
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.common.esia.search.service.PersonSearchService
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.common.service.InitialValueFromService
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalDescriptorService
import ru.gosuslugi.pgu.fs.service.ParticipantService
import spock.lang.Specification

class SnilsComponentTest extends Specification {

    private static SnilsComponent SNILS_COMPONENT
    private static SnilsComponent SNILS_COMPONENT_WITH_DATA
    public static final String CORRECT_SNILS = "661-375-565 12"
    public static final String CORRECT_SNILS_CHILDREN = "123-456-788 63"
    public static final String CORRECT_SNILS_FOR_UNKNOUN_PERSON = "123-456-789 64"
    public static final String INCORRECT_SNILS = "12-34-56 78"
    public static final String INCORRECT_SNILS_WITHOUT_CHECKSUM = "123-456-789 00"
    public static final String OID = "223311"

    private static PersonSearchService personSearch
    private static JsonProcessingService jsonProcessingServiceWithoutData
    private static JsonProcessingService jsonProcessingServiceWithData
    private static ParticipantService participantService
    private static UserPersonalData userPersonalData
    private static InitialValueFromService initialValueFromServiceWithoutData
    private static InitialValueFromService initialValueFromServiceWithData
    private static ErrorModalDescriptorService errorModalDescriptorService

    private static FieldComponent COMPONENT
    private static ScenarioDto SCENARIO_DTO
    static PersonWithAge personWithAge
    private static final String KEY = "key"
    private static final String SUFFIX = "-component.json"
    private static final String DTO_SUFFIX = "-scenarioDto.json"

    def setupSpec() {

        SCENARIO_DTO = new ScenarioDto()
        COMPONENT = JsonProcessingUtil.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), SUFFIX), FieldComponent)
        personWithAge = new PersonWithAge()
        personWithAge.setSnils(CORRECT_SNILS)
        personWithAge.setTrusted(true)
        personWithAge.setOid(OID)

        personSearch = Stub(PersonSearchService) {
            searchOneTrusted(CORRECT_SNILS_FOR_UNKNOUN_PERSON) >> null
            searchOneTrusted(CORRECT_SNILS) >> personWithAge
        }
        jsonProcessingServiceWithoutData = Stub(JsonProcessingService)
        jsonProcessingServiceWithData = Stub(JsonProcessingService) {
            toJson(_) >> personWithAge.toString()
        }
        participantService = Stub(ParticipantService)
        userPersonalData = Stub(UserPersonalData) {
            getUserId() >> Long.valueOf(OID)
        }
        initialValueFromServiceWithoutData = Stub(InitialValueFromService)
        initialValueFromServiceWithData = Stub(InitialValueFromService) {
            getValue(*_) >> CORRECT_SNILS
        }
        errorModalDescriptorService = Stub(ErrorModalDescriptorService)
        SNILS_COMPONENT = new SnilsComponent(personSearch, jsonProcessingServiceWithoutData, participantService,
                userPersonalData, initialValueFromServiceWithoutData, errorModalDescriptorService)
        SNILS_COMPONENT_WITH_DATA = new SnilsComponent(personSearch, jsonProcessingServiceWithData, participantService,
                userPersonalData, initialValueFromServiceWithData, errorModalDescriptorService)
    }

    def 'Get type'() {
        expect:
        SNILS_COMPONENT.getType() == ComponentType.SnilsInput
    }

    def 'Test getInitialValue: get initial value response is value of component'() {
        expect:
        SNILS_COMPONENT.getInitialValue(COMPONENT, SCENARIO_DTO) == ComponentResponse.of(COMPONENT.getValue())
    }

    def 'Test getInitialValue: get initial value response is value from initialValueFromService'() {
        expect:
        SNILS_COMPONENT_WITH_DATA.getInitialValue(COMPONENT, SCENARIO_DTO).get() == CORRECT_SNILS
    }

    def 'Test validateAfterSubmit: empty snils fieldComponent without required'() {
        given:
        def component =
                JsonProcessingUtil.fromJson(
                        JsonFileUtil.getJsonFromFile(this.getClass(), "-withoutRequired" + SUFFIX),
                        FieldComponent
                )
        def entry = Map.entry(KEY, new ApplicantAnswer(false, ""))
        def incorrectAnswers = new HashMap<String, String>()
        SNILS_COMPONENT.validateAfterSubmit(incorrectAnswers, entry, SCENARIO_DTO, component)
        expect:
        incorrectAnswers.isEmpty()
    }

    def 'Test validateAfterSubmit: empty snils fieldComponent with required'() {
        given:
        def map = new HashMap<String, ApplicantAnswer>()
        map.put(KEY, new ApplicantAnswer(false, ""))
        def entry = Collections.checkedMap(map, String.class, ApplicantAnswer.class).entrySet().first()
        def incorrectAnswers = new HashMap<String, String>()
        SNILS_COMPONENT.validateAfterSubmit(incorrectAnswers, entry, SCENARIO_DTO, COMPONENT)
        expect:
        incorrectAnswers.get(KEY) == "Неверное значение СНИЛС"
    }

    def 'Test validateAfterSubmit: empty snils fieldComponent with required with ErrorMessage'() {
        given:
        def component =
                JsonProcessingUtil.fromJson(
                        JsonFileUtil.getJsonFromFile(this.getClass(), "-withErrorMessage" + SUFFIX),
                        FieldComponent
                )
        def map = new HashMap<String, ApplicantAnswer>()
        map.put(KEY, new ApplicantAnswer(false, ""))
        def entry = Collections.checkedMap(map, String.class, ApplicantAnswer.class).entrySet().first()
        def incorrectAnswers = new HashMap<String, String>()
        SNILS_COMPONENT.validateAfterSubmit(incorrectAnswers, entry, SCENARIO_DTO, component)
        expect:
        incorrectAnswers.get(KEY) == "Поле должно содержать 15 символов"
    }

    def 'Test validateAfterSubmit: incorrect snils without checksum'() {
        given:
        def map = new HashMap<String, ApplicantAnswer>()
        map.put(KEY, new ApplicantAnswer(false, INCORRECT_SNILS_WITHOUT_CHECKSUM))
        def entry = Collections.checkedMap(map, String.class, ApplicantAnswer.class).entrySet().first()
        def incorrectAnswers = new HashMap<String, String>()
        SNILS_COMPONENT.validateAfterSubmit(incorrectAnswers, entry, SCENARIO_DTO, COMPONENT)
        expect:
        incorrectAnswers.get(KEY) == "Неверное значение СНИЛС"
    }

    def 'Test validateAfterSubmit: incorrect snils'() {
        given:
        def map = new HashMap<String, ApplicantAnswer>()
        map.put(KEY, new ApplicantAnswer(false, INCORRECT_SNILS))
        def entry = Collections.checkedMap(map, String.class, ApplicantAnswer.class).entrySet().first()
        def incorrectAnswers = new HashMap<String, String>()
        SNILS_COMPONENT.validateAfterSubmit(incorrectAnswers, entry, SCENARIO_DTO, COMPONENT)
        expect:
        incorrectAnswers.get(KEY) == "Неверное значение СНИЛС"
    }

    def 'Test validateAfterSubmit: correct snils'() {
        given:
        def map = new HashMap<String, ApplicantAnswer>()
        map.put(KEY, new ApplicantAnswer(false, CORRECT_SNILS))
        def entry = Collections.checkedMap(map, String.class, ApplicantAnswer.class).entrySet().first()
        def incorrectAnswers = new HashMap<String, String>()
        SNILS_COMPONENT.validateAfterSubmit(incorrectAnswers, entry, SCENARIO_DTO, COMPONENT)
        expect:
        incorrectAnswers.get(KEY) == "Нельзя ввести собственный СНИЛС"
    }

    def 'Test validateAfterSubmit: correct snils with false attr validationOwnSnils'() {
        given:
        def component =
                JsonProcessingUtil.fromJson(
                        JsonFileUtil.getJsonFromFile(this.getClass(), "-withFalseValidationOwnSnils" + SUFFIX),
                        FieldComponent
                )
        def map = new HashMap<String, ApplicantAnswer>()
        map.put(KEY, new ApplicantAnswer(false, CORRECT_SNILS))
        def entry = Collections.checkedMap(map, String.class, ApplicantAnswer.class).entrySet().first()
        def incorrectAnswers = new HashMap<String, String>()
        SNILS_COMPONENT_WITH_DATA.validateAfterSubmit(incorrectAnswers, entry, SCENARIO_DTO, component)
        expect:
        incorrectAnswers.isEmpty()
    }

    def 'Test validateAfterSubmit: correct snils unknoun person'() {
        given:
        def map = new HashMap<String, ApplicantAnswer>()
        map.put(KEY, new ApplicantAnswer(false, CORRECT_SNILS_FOR_UNKNOUN_PERSON))
        def entry = Collections.checkedMap(map, String.class, ApplicantAnswer.class).entrySet().first()
        def incorrectAnswers = new HashMap<String, String>()
        SNILS_COMPONENT_WITH_DATA.validateAfterSubmit(incorrectAnswers, entry, SCENARIO_DTO, COMPONENT)
        expect:
        incorrectAnswers.isEmpty()
    }

    def 'Test validateAfterSubmit: correct snils with same children snils'() {
        given:
        def component =
                JsonProcessingUtil.fromJson(
                        JsonFileUtil.getJsonFromFile(this.getClass(), "-withCheckRepeatedChildrenSnils" + SUFFIX),
                        FieldComponent
                )
        def scenarioDto =
                JsonProcessingUtil.fromJson(
                        JsonFileUtil.getJsonFromFile(this.getClass(), DTO_SUFFIX),
                        ScenarioDto.class
                )
        def map = new HashMap<String, ApplicantAnswer>()
        map.put(KEY, new ApplicantAnswer(false, CORRECT_SNILS_CHILDREN))
        def entry = Collections.checkedMap(map, String.class, ApplicantAnswer.class).entrySet().first()
        def incorrectAnswers = new HashMap<String, String>()
        SNILS_COMPONENT_WITH_DATA.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, component)
        expect:
        incorrectAnswers.get(KEY) == "У детей не могут быть одинаковые СНИЛС"
    }

    def 'Test validateAfterSubmit: correct snils with different children snils'() {
        given:
        def component =
                JsonProcessingUtil.fromJson(
                        JsonFileUtil.getJsonFromFile(this.getClass(), "-withCheckRepeatedChildrenSnils" + SUFFIX),
                        FieldComponent
                )
        def scenarioDto =
                JsonProcessingUtil.fromJson(
                        JsonFileUtil.getJsonFromFile(this.getClass(), DTO_SUFFIX),
                        ScenarioDto
                )
        def map = new HashMap<String, ApplicantAnswer>()
        map.put(KEY, new ApplicantAnswer(false, CORRECT_SNILS))
        def entry = Collections.checkedMap(map, String.class, ApplicantAnswer.class).entrySet().first()
        def incorrectAnswers = new HashMap<String, String>()
        SNILS_COMPONENT_WITH_DATA.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, component)
        expect:
        incorrectAnswers.get(KEY) == "Нельзя ввести собственный СНИЛС"
    }
}
