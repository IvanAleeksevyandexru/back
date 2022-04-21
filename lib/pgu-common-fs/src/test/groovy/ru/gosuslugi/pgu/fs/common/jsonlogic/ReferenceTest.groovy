package ru.gosuslugi.pgu.fs.common.jsonlogic

import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto

class ReferenceTest extends JsonLogicSpec {

    def "All answer references  exist in scenarioDto"() {
        given:
        String json = JsonFileUtil.getJsonFromFile(this.getClass(), "-reference.json")
        def scenarioDto = new ScenarioDto(applicantAnswers: [ai7: new ApplicantAnswer(value: value)])
        when:
        Object result = jsonLogic.calculate(json, scenarioDto)
        then:
        result == expectedResult
        where:
        value | expectedResult
        "0"   | 'ноль'
        "1"   | 'один'
        "2"   | 'много'
        "10"  | 'много'
    }

    def "Answer reference doesn't exist in scenarioDto, use default value"() {
        given:
        String json = JsonFileUtil.getJsonFromFile(this.getClass(), "-reference.json")
        def scenarioDto = new ScenarioDto(applicantAnswers: [ai6: new ApplicantAnswer(value: value)])
        when:
        Object result = jsonLogic.calculate(json, scenarioDto)
        then:
        result == expectedResult
        where:
        value | expectedResult
        "0"   | 'много'
        "1"   | 'много'
        "2"   | 'много'
        "10"  | 'много'
    }

    def "answer returns like json string"() {
        given:
        String json = "{\"value\": \"answer.ai5.value\"}}"
        def scenarioDto = new ScenarioDto(applicantAnswers: [ai5: new ApplicantAnswer(value: value)])
        when:
        Object result = jsonLogic.calculate(json, scenarioDto)
        then:
        result == expectedResult
        where:
        value | expectedResult
        '{\\"field1\\":\\"value1\\",\\"field2\\":\\"value2\\"}'   | '{\\"field1\\":\\"value1\\",\\"field2\\":\\"value2\\"}'
        '[{\\"field1\\":\\"value1\\",\\"field2\\":\\"value2\\"}]' | '[{\\"field1\\":\\"value1\\",\\"field2\\":\\"value2\\"}]'
        "4"     | '4'
        "пять"  | 'пять'
    }
}
