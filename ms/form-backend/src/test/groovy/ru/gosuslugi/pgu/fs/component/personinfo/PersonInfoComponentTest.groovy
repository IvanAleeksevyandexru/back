package ru.gosuslugi.pgu.fs.component.personinfo

import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import ru.gosuslugi.pgu.fs.component.personinfo.model.PersonAge
import ru.gosuslugi.pgu.fs.component.personinfo.model.PersonInfoDto
import spock.lang.Specification

class PersonInfoComponentTest extends Specification {

    private static final String COMPONENT_SUFFIX = "-component.json"
    private static final String CYCLED_COMPONENT_SUFFIX = "-cycledComponent.json"
    private static final String EXTERNAL_DATA_SUFFIX = "-externalData.json"
    private static final String PERSON_INFO_DTO_DATA_SUFFIX = "-personInfoDto.json"
    private static final String SCENARIO_DTO_SUFFIX = "-scenarioDto.json"
    private static final String SERVICE_DESCRIPTOR_SUFFIX = "-serviceDescriptor.json"

    PersonInfoComponent personInfoComponent

    def "Chek method getType returns the correct value"() {
        given:
        personInfoComponent = new PersonInfoComponent()

        expect:
        personInfoComponent.getType() == ComponentType.PersonInfo
    }

    def "Chek getCycledInitialValue with correctly data"() {
        given:
        PersonAgeService personAgeServiceMock = Mock(PersonAgeService)
        personAgeServiceMock.createPersonAge(_) >> new PersonAge(0, 10)
        personInfoComponent = new PersonInfoComponent(personAgeServiceMock)
        ComponentTestUtil.setAbstractComponentServices(personInfoComponent)
        def fieldComponent = readComponentFromFile(COMPONENT_SUFFIX)
        def externalData = readExternalDataFromFile(EXTERNAL_DATA_SUFFIX)
        def personInfoDto = readPersonDtoFromFile(PERSON_INFO_DTO_DATA_SUFFIX)
        def correctComponentResponse = ComponentResponse.of(personInfoDto)

        when:
        def cycledInitialValue = personInfoComponent.getCycledInitialValue(fieldComponent, externalData)

        then:
        cycledInitialValue == correctComponentResponse
    }

    def "Chek getInitialValue without cycledAttrs"() {
        given:
        personInfoComponent = new PersonInfoComponent()
        def fieldComponent = readComponentFromFile(COMPONENT_SUFFIX)
        def scenarioDto = readScenarioDtoFromFile(SCENARIO_DTO_SUFFIX)
        def serviceDescriptor = readServiceDescriptorFromFile(SERVICE_DESCRIPTOR_SUFFIX)
        def correctComponentResponse = ComponentResponse.of(null)

        when:
        def initialValue = personInfoComponent.getInitialValue(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        initialValue == correctComponentResponse
    }

    def "Chek getInitialValue with cycledAttrs"() {
        given:
        PersonAgeService personAgeServiceMock = Mock(PersonAgeService)
        personInfoComponent = new PersonInfoComponent(personAgeServiceMock)
        ComponentTestUtil.setAbstractComponentServices(personInfoComponent)
        def fieldComponent = readComponentFromFile(CYCLED_COMPONENT_SUFFIX)
        def scenarioDto = readScenarioDtoFromFile(SCENARIO_DTO_SUFFIX)
        def serviceDescriptor = readServiceDescriptorFromFile(SERVICE_DESCRIPTOR_SUFFIX)
        def correctComponentResponse = ComponentResponse.of(new PersonInfoDto())

        when:
        def initialValue = personInfoComponent.getInitialValue(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        initialValue == correctComponentResponse
    }

    def readComponentFromFile(suffix) {
        return JsonProcessingUtil.fromJson(
                JsonFileUtil.getJsonFromFile(this.getClass(), suffix),
                FieldComponent
        )
    }

    def readExternalDataFromFile(suffix) {
        return JsonProcessingUtil.fromJson(
                JsonFileUtil.getJsonFromFile(this.getClass(), suffix),
                Map
        )
    }

    def readPersonDtoFromFile(suffix) {
        return JsonProcessingUtil.fromJson(
                JsonFileUtil.getJsonFromFile(this.getClass(), suffix),
                PersonInfoDto
        )
    }

    def readScenarioDtoFromFile(suffix) {
        return JsonProcessingUtil.fromJson(
                JsonFileUtil.getJsonFromFile(this.getClass(), suffix),
                ScenarioDto
        )
    }

    def readServiceDescriptorFromFile(suffix) {
        return JsonProcessingUtil.fromJson(
                JsonFileUtil.getJsonFromFile(this.getClass(), suffix),
                ServiceDescriptor
        )
    }
}

