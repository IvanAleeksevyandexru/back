package ru.gosuslugi.pgu.fs.component.dictionary


import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.common.service.ProtectedFieldService
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionary
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionaryItem
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterRequest
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService
import spock.lang.Specification

import static ru.gosuslugi.pgu.components.ComponentAttributes.VALUE_NOT_FOUND_MESSAGE

class InformationCenterFssComponentTest extends Specification {
    private static final ADDRESS_SUFFIX = "-address.json"
    private static final int REGION_CODE = 78
    private static final String TO_FSS_DICTIONARY_NAME = "FSS_TO"
    private static final String FSS_NAME = "NAME"
    private static final String TEST_NAME = "TEST"

    NsiDictionaryService nsiDictionaryService
    InformationCenterFssComponent informationCenterFssComponent

    void setup() {
        nsiDictionaryService = Mock(NsiDictionaryService)
        informationCenterFssComponent = new InformationCenterFssComponent(nsiDictionaryService,
                new ParseAttrValuesHelper(Mock(VariableRegistry), Mock(JsonProcessingService), Mock(ProtectedFieldService)))
        ComponentTestUtil.setAbstractComponentServices(informationCenterFssComponent)
    }


    def "Test not found fss center by region code"() {
        given:
        def fieldComponent = createFieldComponent()
        def scenarioDto = createScenarioDto()

        when:
        nsiDictionaryService.getDictionary(_ as String, _ as NsiDictionaryFilterRequest) >> null
        informationCenterFssComponent.preProcess(fieldComponent, scenarioDto)

        then:
        FormBaseException formBaseException =
                thrown(FormBaseException)
        formBaseException.message == "Не найдено подразделение по коду региона ${REGION_CODE}"
    }

    def "Test returning more than one fss center for region"() {
        given:
        def fieldComponent = createFieldComponent()
        def scenarioDto = createScenarioDto()

        when:
        nsiDictionaryService.getDictionary(_ as String, _ as NsiDictionaryFilterRequest) >>
                new NsiDictionary(items: [new NsiDictionaryItem(value: '1', title: 'Центр 1'),
                                          new NsiDictionaryItem(value: '2', title: 'Центр 2')])
        informationCenterFssComponent.preProcess(fieldComponent, scenarioDto)

        then:
        FormBaseException formBaseException = thrown(FormBaseException)
        formBaseException.message == "Должно возвращаться всегда единственное подразделение по коду региона ${REGION_CODE}"
    }

    def "Test correct pre-process"() {
        given:
        def fieldComponent = createFieldComponent()
        def scenarioDto = createScenarioDto()

        when:
        nsiDictionaryService.getDictionary(_ as String, _ as NsiDictionaryFilterRequest) >>
                new NsiDictionary(items: [new NsiDictionaryItem(value: '1', title: 'Центр 1')])
        informationCenterFssComponent.preProcess(fieldComponent, scenarioDto)

        then:
        fieldComponent.getAttrs().containsKey("simple")
    }

    def "Test validate after submit without dictionary items"() {
        given:
        def fieldComponent = createFieldComponent()
        def scenarioDto = createScenarioDto()
        Map<String, String> incorrectAnswers = new HashMap<>()
        Map.Entry<String, ApplicantAnswer> entry = new MapEntry("address", new ApplicantAnswer('value': '{"title":"title"}'))

        when:
        nsiDictionaryService.getDictionaryItemByValue(_ as String, _ as String, _ as String) >> Optional.empty()
        informationCenterFssComponent.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent)

        then:
        incorrectAnswers.getOrDefault('address', null) == String.format(VALUE_NOT_FOUND_MESSAGE, TO_FSS_DICTIONARY_NAME)
    }

    def "Test validate after submit with wrong dictionary item"() {
        given:
        def fieldComponent = createFieldComponent()
        def scenarioDto = createScenarioDto()
        Map<String, String> incorrectAnswers = new HashMap<>()
        Map.Entry<String, ApplicantAnswer> entry = new MapEntry("address", new ApplicantAnswer('value': '{"title":"title"}'))

        when:
        nsiDictionaryService.getDictionaryItemByValue(_ as String, _ as String, _ as String) >> Optional.ofNullable(new NsiDictionaryItem())
        informationCenterFssComponent.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent)

        then:
        incorrectAnswers.getOrDefault('address', null) ==
                "Выбранное значение не совпадает со значением в справочнике ${TO_FSS_DICTIONARY_NAME}"
    }

    def "Test correct validate after submit"() {
        given:
        def fieldComponent = createFieldComponent()
        def scenarioDto = createScenarioDto()
        Map<String, String> incorrectAnswers = new HashMap<>()
        Map.Entry<String, ApplicantAnswer> entry = new MapEntry("address", new ApplicantAnswer('value': "{'title':'${TEST_NAME}'}"))
        NsiDictionaryItem nsiDictionaryItem = new NsiDictionaryItem()
        nsiDictionaryItem.setAttributeValues(Map.of(FSS_NAME, TEST_NAME))

        when:
        informationCenterFssComponent.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent)

        then:
        1 * nsiDictionaryService.getDictionaryItemByValue(_ as String, _ as String, _ as String) >> Optional.ofNullable(nsiDictionaryItem)
        incorrectAnswers.isEmpty()
    }

    private FieldComponent createFieldComponent() {
        def fieldComponent = new FieldComponent(type: ComponentType.InformationCenterFss)
        fieldComponent.attrs = [addressString: [type: 'REF', value: 'address']]
        return fieldComponent
    }

    private ScenarioDto createScenarioDto() {
        def scenarioDto = new ScenarioDto()
        scenarioDto.applicantAnswers = [address: new ApplicantAnswer('value': JsonFileUtil.getJsonFromFile(this.getClass(), ADDRESS_SUFFIX))]
        return scenarioDto
    }
}
