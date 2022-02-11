package ru.gosuslugi.pgu.fs.component

import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.component.gender.DictionaryGenderComponent
import ru.gosuslugi.pgu.fs.service.DictionaryFilterService
import spock.lang.Specification

class DictionaryGenderComponentTest extends Specification {

    DictionaryGenderComponent dictionaryGenderComponent
    DictionaryFilterService dictionaryFilterService

    void setup() {
        dictionaryFilterService = Mock(DictionaryFilterService)
        dictionaryGenderComponent = new DictionaryGenderComponent(dictionaryFilterService)
        ComponentTestUtil.setAbstractComponentServices(dictionaryGenderComponent)
    }

    def "Test getInitailValue"() {
        given:
        FieldComponent fieldComponent = new FieldComponent()
        ScenarioDto scenarioDto = new ScenarioDto()
        dictionaryFilterService.getInitialValue(fieldComponent, scenarioDto) >> initial

        when:
        ComponentResponse response = dictionaryGenderComponent.getInitialValue(fieldComponent, scenarioDto)

        then:
        response == expectedResponse

        where:
        initial                                   | expectedResponse
        [] as HashMap<String, Object>             | ComponentResponse.empty()
        [key: "value"] as HashMap<String, Object> | ComponentResponse.of(JsonProcessingUtil.toJson([key: "value"] as HashMap<String, Object>))
    }

    def "Test preloadComponent"() {
        given:
        FieldComponent fieldComponent = new FieldComponent()
        ScenarioDto scenarioDto = new ScenarioDto()

        when:
        dictionaryGenderComponent.preloadComponent(fieldComponent, scenarioDto)

        then:
        1 * dictionaryFilterService.preloadComponent(fieldComponent, scenarioDto, _)
    }

    def "Test validateAfterSubmit"() {
        given:
        FieldComponent fieldComponent = new FieldComponent()
        ScenarioDto scenarioDto = new ScenarioDto()

        when:
        dictionaryGenderComponent.validateAfterSubmit(Collections.emptyMap(), entry, scenarioDto, fieldComponent)

        then:
        validateAfterSubmitCallCount * dictionaryFilterService.validateAfterSubmit(Collections.emptyMap(), entry, scenarioDto, fieldComponent, _)

        where:
        entry                                         | validateAfterSubmitCallCount
        ComponentTestUtil.answerEntry("key", "value") | 1
        null                                          | 0
    }
}
