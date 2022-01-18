package unit.ru.gosuslugi.pgu.fs.service.impl

import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.Expression
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.LinkedValue
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.component.ComponentTestUtil
import spock.lang.Specification

class LinkedValuesServiceImplSpec extends Specification {

    static service
    static jsonProcessingService

    def setupSpec() {
        jsonProcessingService = new JsonProcessingServiceImpl(JsonProcessingUtil.getObjectMapper())

        service = ComponentTestUtil.getLinkedValuesService(jsonProcessingService)
    }

    def 'LinkedValue with SPEL converter should set an argument based on expression and reference value type'() {
        given:
        def component = new FieldComponent(
                label: 'label',
                value: 'value',
                linkedValues: [
                        new LinkedValue(
                                source: ["path": "ai7", "converter": "SPEL", "expression": expression, "refValueType": refValueType],
                                argument: 'TEXT',
                                defaultValue: 'вашим детям',
                                expressions: [
                                        new Expression(when: '1', then: 'вашему ребенку'),
                                ]
                        )])
        def scenarioDto = new ScenarioDto(applicantAnswers: [ai7: new ApplicantAnswer(value: value)])

        when:
        service.fillLinkedValues(component, scenarioDto)

        then:
        component.arguments.get('TEXT') == expectedResult

        where:
        value                                                   | expression | refValueType | expectedResult
        "[{\"a\":\"1\",\"b\":\"2\"},{\"a\":\"3\",\"b\":\"4\"}]" | 'size()'   | 'List'       | 'вашим детям'
        "[{\"a\":\"1\",\"b\":\"2\"}]"                           | 'size()'   | 'List'       | 'вашему ребенку'
    }

    def 'LinkedValue with string source should set an argument based on value from source'() {
        given:
        def component = new FieldComponent(
                label: 'label',
                value: 'value',
                linkedValues: [
                        new LinkedValue(
                                source: 'ai7',
                                argument: 'TEXT',
                                defaultValue: 'много',
                                isJsonSource: false,
                                expressions: [
                                        new Expression(when: '0', then: 'ноль'),
                                        new Expression(when: '1', then: 'один'),
                                ]
                        )])
        def scenarioDto = new ScenarioDto(applicantAnswers: [ai7: new ApplicantAnswer(value: value)])

        when:
        service.fillLinkedValues(component, scenarioDto)

        then:
        component.arguments.get('TEXT') == expectedResult

        where:
        value | expectedResult
        "0"   | 'ноль'
        "1"   | 'один'
        "2"   | 'много'
        "10"  | 'много'

    }

    def 'LinkedValue should set an argument based on jsonLogic'() {
        given:
        def component = jsonProcessingService.fromJson(JsonFileUtil.getJsonFromFile(this.getClass(), "-jsonLogic.json"), FieldComponent.class)

        def scenarioDto = new ScenarioDto(applicantAnswers: [ai7: new ApplicantAnswer(value: value)])

        when:
        service.fillLinkedValues(component, scenarioDto)

        then:
        component.arguments.get('TEXT') == expectedResult

        where:
        value | expectedResult
        "0"   | 'ноль'
        "1"   | 'один'
        "2"   | 'много'
        "10"  | 'много'
    }

    def 'LinkedValue should set an argument based on jsonPath expression'() {
        given:
        def scenarioDto = new ScenarioDto(applicantAnswers: [
                ai7: [
                        value: '{"items":[{"name":"Person-1","status":"Inactive","value":{"label":"Имя","text":"Вася"},"salary":2000},{"name":"Person-2","status":"Inactive","value":{"label":"Фамилия","text":"Пупкин"},"salary":2100},{"name":"Person-3","status":"Active","value":{"label":"Должность","text":"Заместитель"},"salary":1520},{"name":"Person-4","status":"Inactive","value":{"label":"Имя","text":"Вася"},"salary":990},{"name":"Person-5","status":"Inactive","value":{"label":"Должность","text":"Босс"},"salary":2500,"attributes":[]},{"name":"Admin","isAdmin":true,"status":"Inactive","value":{"label":"Должность","text":"Босс"},"salary":2500}]}'
                ] as ApplicantAnswer
        ])
        def component = new FieldComponent(
                label: 'label',
                value: 'value',
                linkedValues: [
                        new LinkedValue(
                                argument: 'TEXT',
                                jsonPath: jsonPath,
                                defaultValue: defaultValue
                        )])
        when:
        service.fillLinkedValues(component, scenarioDto)

        then:
        component.getArgument('TEXT') == result

        where:
        jsonPath                                                   | defaultValue || result
        'ai7.items[0]'                                             | null         || '{name=Person-1, status=Inactive, value={label=Имя, text=Вася}, salary=2000}'
        'ai7.items[:2]'                                            | null         || '[{"name":"Person-1","status":"Inactive","value":{"label":"Имя","text":"Вася"},"salary":2000},{"name":"Person-2","status":"Inactive","value":{"label":"Фамилия","text":"Пупкин"},"salary":2100}]'
        'ai7.items[:2].salary'                                     | null         || '[2000,2100]'
        'ai7.items[0].name'                                        | null         || 'Person-1'
        'ai7.items[-1:].name'                                      | null         || '["Admin"]'
        'ai7.items[2].status'                                      | null         || 'Active'
        'ai7..items[?(@.attributes)].salary'                       | null         || '[2500]'
        'ai7..items[?(@.salary > 2400 && @.isAdmin == true)].name' | null         || '["Admin"]'
        'test'                                                     | null         || ''
        'test.name'                                                | 'default'    || 'default'
        null                                                       | null         || ''
        null                                                       | 'default'    || 'default'
    }

    def 'LinkedValue with numeric source should set an argument based on value from source'() {
        given:
        def component = new FieldComponent(
                label: 'label',
                value: 'value',
                linkedValues: [
                        new LinkedValue(
                                source: 'ai7.num',
                                argument: 'number',
                        )])
        def scenarioDto = new ScenarioDto(applicantAnswers: [ai7: new ApplicantAnswer(value: value)])

        when:
        service.fillLinkedValues(component, scenarioDto)

        then:
        component.arguments.get('number') == expectedResult

        where:
        value | expectedResult
        "{\"num\": 100}"             | "100"
        "{\"num\": 100.123}"         | "100.123"
        "{\"num\": 1000000000000}"   | "1000000000000"
    }
}
