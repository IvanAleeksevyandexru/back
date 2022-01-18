package ru.gosuslugi.pgu.fs.common.jsonlogic.operator

import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.fs.common.jsonlogic.JsonLogicSpec

class EqualityOperatorTest extends JsonLogicSpec {

    def "Same value same type"() {
        when:
        Object value = jsonLogic.calculate(json, new ScenarioDto())
        then:
        value == expectedResult
        where:
        json                            | expectedResult
        "{\"==\" : [1, 1]}"             | true
        "{\"==\" : [1, \"1\"]}"         | true
        "{\"==\" : [[], false]}"        | true
        "{\"==\" : [\" \", 0]}"         | true
        "{\"==\" : [false, \"false\"]}" | true
    }

    def "String and date equals"() {
        given:
        String json = JsonFileUtil.getJsonFromFile(this.getClass(), "-stringAndDate.json")
        when:
        Object value = jsonLogic.calculate(json, new ScenarioDto())
        then:
        value == true
    }

    def "Two dates equals"() {
        given:
        String json = JsonFileUtil.getJsonFromFile(this.getClass(), "-twoDates.json")
        when:
        Object value = jsonLogic.calculate(json, new ScenarioDto())
        then:
        value == true
    }

}
