package ru.gosuslugi.pgu.fs.component.confirm

import ru.atc.carcass.security.rest.model.person.PersonDoc
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.components.FieldComponentUtil
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.esia.impl.EsiaRestContactDataClientImpl
import spock.lang.Specification

import java.util.stream.Stream

class ConfirmPersonalPolicyChangeComponentTest extends Specification {
    private static final String COMPONENT_SUFFIX = "-component.json"
    private static final String SERIES = "series";
    private static final String NUMBER = "number";

    ConfirmPersonalPolicyChange confirmPersonalPolicyChangeComponent
    EsiaRestContactDataClientImpl esiaRestContactDataClient
    UserPersonalData userPersonalData
    ScenarioDto scenarioDto

    void setup() {
        esiaRestContactDataClient = Mock(EsiaRestContactDataClientImpl)
        userPersonalData = Mock(UserPersonalData)
        confirmPersonalPolicyChangeComponent = new ConfirmPersonalPolicyChange(esiaRestContactDataClient, userPersonalData)
        scenarioDto = new ScenarioDto()
    }

    def "Test getType return proper value"() {
        expect:
        confirmPersonalPolicyChangeComponent.getType() == ComponentType.ConfirmPersonalPolicyChange
    }

    def "Test getInitialValue returns data depending on component fields"() {
        given:
        def fieldComponent = createComponentWithFields(componentFields)
        userPersonalData.getOmsDocument() >> Optional.of(new PersonDoc(series: "2", number: "1"))

        when:
        def componentResponse = confirmPersonalPolicyChangeComponent.getInitialValue(fieldComponent, scenarioDto)

        then:
        componentResponse.get().getNumber() == number
        componentResponse.get().getSeries() == series

        where:
        componentFields            || number || series
        SERIES                     || null   || "2"
        NUMBER                     || "1"    || null
        (String[])[SERIES, NUMBER] || "1"    || "2"
    }

    def createComponentWithFields(String... fieldNames) {
        FieldComponent fieldComponent = readComponentFromFile(COMPONENT_SUFFIX)
        def fields = (List) fieldComponent.getAttrs().get(FieldComponentUtil.FIELDS_KEY)
        Stream.of(fieldNames)
                .map({ field -> [fieldName: field] })
                .forEach(fields.&add)
        return fieldComponent
    }

    def readComponentFromFile(prefix) {
        return JsonProcessingUtil.fromJson(
                JsonFileUtil.getJsonFromFile(this.getClass(), prefix),
                FieldComponent
        )
    }
}
