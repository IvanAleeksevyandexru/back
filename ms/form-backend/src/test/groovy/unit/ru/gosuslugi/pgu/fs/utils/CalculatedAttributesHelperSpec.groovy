package unit.ru.gosuslugi.pgu.fs.utils

import com.jayway.jsonpath.DocumentContext
import org.mockito.Mockito
import ru.gosuslugi.pgu.components.descriptor.placeholder.PlaceholderContext
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.DisplayRequest
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.ServiceInfoDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.fs.common.service.ComponentReferenceService
import ru.gosuslugi.pgu.fs.common.variable.ServiceIdVariable
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper
import spock.lang.Specification

class CalculatedAttributesHelperSpec extends Specification {

    CalculatedAttributesHelper helper
    ComponentReferenceService componentReferenceService = Mockito.mock(ComponentReferenceService.class)

    FieldComponent component = new FieldComponent(attrs: ["key": "value"])
    ScenarioDto scenarioDto = new ScenarioDto(
            orderId: 1234,
            display: new DisplayRequest(components: [ component ]),
            serviceInfo: new ServiceInfoDto(
                    queryParams: ["value": "queryParameter"]
            )
    )

    def setup() {
        helper = new CalculatedAttributesHelper(
                Stub(ServiceIdVariable) as ServiceIdVariable,
                new ParseAttrValuesHelper(null, null, null),
                componentReferenceService)
        helper.postConstruct()
    }

    def 'Can get calculated value'() {
        given:
        Map item = [attributeName: 'test1', condition: 'EQUALS', value: '', expr: expr, valueType: 'calc']
        String exprAttrName = 'expr'
        ScenarioDto scenarioDto = new ScenarioDto(currentValue: [q1 : new ApplicantAnswer(value: 'Нового образца'),
                                                                 q7 : new ApplicantAnswer(value: 'Оформить паспорт нового образца'),
                                                                 pd1: new ApplicantAnswer(value: '{"storedValues":{"fullName":"Микки Маус"}}'),
                                                                 pd4: new ApplicantAnswer(value: '{"regAddr":{"regionType":"город","regionShortType":"г","region":"Москва","cityType":"","cityShortType":"","city":"","districtType":"","districtShortType":"","district":"","townType":"","townShortType":"","town":"","inCityDistType":"","inCityDistShortType":"","inCityDist":"","streetType":"улица","streetShortType":"ул","street":"Большая Оленья","additionalAreaType":"","additionalAreaShortType":"","additionalArea":"","additionalStreetType":"","additionalStreetShortType":"","additionalStreet":"","houseType":"дом","houseShortType":"д","house":"15","houseCheckbox":false,"houseCheckboxClosed":false,"building1Type":"","building1ShortType":"","building1":"","building2Type":"строение","building2ShortType":"стр","building2":"1","apartmentType":"","apartmentShortType":"","apartment":"","apartmentCheckbox":true,"apartmentCheckboxClosed":false,"index":"107014","geoLat":"55.8092016","geoLon":"37.6914253","fullAddress":"107014, г Москва, ул Большая Оленья, д 15 стр 1","lat":"55.8092016","lng":"37.6914253","fiasCode":"68c2c165-62ec-4a2b-9b2b-2e95de4fd783","okato":"45263591000","oktmo":"45315000","hasErrors":0,"kladrCode":"7700000000021230025","regionCode":"77"}}')])
        when:
        String result = helper.getCalculatedValue(item, Stub(FieldComponent), scenarioDto, exprAttrName)

        then:
        result == expectedResult

        where:
        expectedResult | expr
        '10000101005'  | "'10000101005'"
        'Микки Маус'   | '$pd1.value.storedValues.fullName'
        'true'         | '$q1.value == \'Нового образца\''
        'false'        | '$q1.value != \'Нового образца\''
        '100'          | '($q1.value == \'Нового образца\' && $q7.value == \'Оформить паспорт нового образца\') ? 100 : 200'
        '200'          | '($q1.value == \'Старого образца\' || $q7.value == \'Оформить паспорт старого образца\') ? 100 : 200'
        'true'         | '$q1.value == \'Нового образца\' || $q7.value == \'Оформить загранпаспорт нового образца\''
        'Иное'         | ['$q1.value == \'Старого образца\' ? \'Старый\' : \'\'',
                          '$q1.value == \'Древнего образца\' ? \'Древний\' : \'Иное\'']
        'Новый'        | ['$q1.value == \'Старого образца\' ? \'Старый\' : \'\'',
                          '$q1.value == \'Нового образца\' ? \'Новый\' : \'Иное\'']
        'smaller'      | '$pd4.value.regAddr.okato <= 47000000000L ? "smaller" : "bigger"'
        'bigger'       | '$pd4.value.regAddr.okato <= 43000000000L ? "smaller" : "bigger"'
        'smaller'      | '$pd4.value.regAddr.okato <= 47000000000.0 ? "smaller" : "bigger"'
        'bigger'       | '$pd4.value.regAddr.okato <= 43000000000.0 ? "smaller" : "bigger"'
        'smaller'      | '($pd4.value.regAddr.okato).compareTo("47000000000") <= 0 ? "smaller" : "bigger"'
        'bigger'       | '($pd4.value.regAddr.okato).compareTo("43000000000") <= 0 ? "smaller" : "bigger"'
        'true'         | '($pd4.value.regAddr.geoLat).compareTo($pd4.value.regAddr.geoLon) > 0'
    }

    def 'Can get value by ref'() {

        given:

        PlaceholderContext placeholderContext = PlaceholderContext.builder().build()
        DocumentContext[] documentContexts = []

        Map<String, Object> attributes = [
                "attributeName": "LIC_DEPARTMENT_OGRN",
                "condition": "EQUALS",
                "value": "\${linkedValueKey}",
                "valueType": "ref"
        ]

        when:

        Mockito.when(componentReferenceService.buildPlaceholderContext(component, scenarioDto)).thenReturn(placeholderContext)
        Mockito.when(componentReferenceService.getContexts(scenarioDto)).thenReturn(documentContexts)
        Mockito.when(componentReferenceService.getValueByContext(
                Mockito.eq("\${linkedValueKey}"),
                Mockito.any(),
                Mockito.any(),
                Mockito.any()
        )).thenReturn("resolvedValue")

        helper.getAllCalculatedValues([attributes], component, scenarioDto)

        then:
        attributes["value"] == "resolvedValue"
        attributes["valueType"] == "ref"

    }

    def 'Must set correct valueType'() {

        given:
        def attributes = [
                "attributeName": "attrName",
                "condition": "ANY",
                "value": "value",
                "expr": "orderId",
                "valueType": valueType
        ]

        when:
        helper.getAllCalculatedValues([attributes], component, scenarioDto)

        then:
        attributes["valueType"] == convertedValueType

        where:
        valueType     | convertedValueType
        "value"       | "value"
        "calc"        | "preset"
        "ref"         | "ref"
        "root"        | "root"
        "preset"      | "preset"
        "protected"   | "protected"
        "serviceInfo" | "serviceInfo"
        "argument"    | "argument"

    }
}
