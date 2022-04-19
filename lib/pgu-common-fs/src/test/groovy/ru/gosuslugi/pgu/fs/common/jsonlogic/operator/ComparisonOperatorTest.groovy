package ru.gosuslugi.pgu.fs.common.jsonlogic.operator

import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.fs.common.jsonlogic.JsonLogicSpec

class ComparisonOperatorTest extends JsonLogicSpec {

    def "Compare values"() {
        when:
        Object value = jsonLogic.calculate(json, new ScenarioDto())
        then:
        value == expected
        where:
        json               | expected
        "{\">\" : [2, 1]}" | true
        "{\">\" : [\"2\", 1]}" | true
        "{\"<=\" : [1, 1]}" | true
        "{\"<\" : [1, 3]}" | true
        "{\"<\" : [1, \"3\"]}" | true

    }

    def "String > date"() {
        given:
        String json = JsonFileUtil.getJsonFromFile(this.getClass(), "-stringAndDate.json")
        when:
        Object value = jsonLogic.calculate(json, new ScenarioDto())
        then:
        value == true
    }

    def "Date < date"() {
        given:
        String json = JsonFileUtil.getJsonFromFile(this.getClass(), "-twoDates.json")
        when:
        Object value = jsonLogic.calculate(json, new ScenarioDto())
        then:
        value == true
    }
}
