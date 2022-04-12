package ru.gosuslugi.pgu.fs.common.jsonlogic.operator

import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.fs.common.jsonlogic.JsonLogicSpec


class IfOperatorTest extends JsonLogicSpec {

    def "Simple true condition"() {
        when:
        Object value = jsonLogic.calculate("{\"if\" : [true, \"yes\", \"no\"]}", new ScenarioDto())
        then:
        value == "yes"
    }

    def "Simple false condition"() {
        when:
        Object value = jsonLogic.calculate("{\"if\" : [false, \"yes\", \"no\"]}", new ScenarioDto())
        then:
        value == "no"
    }

    def "If else if else"() {
        given:
        String json = JsonFileUtil.getJsonFromFile(this.getClass(), "-ifElseIfElse.json")
        when:
        Object value = jsonLogic.calculate(json, new ScenarioDto())
        then:
        value == 'liquid'
    }

    def "Multiple conditions"() {
        given:
        String json = JsonFileUtil.getJsonFromFile(this.getClass(), "-multipleConditions.json")
        when:
        Object value = jsonLogic.calculate(json, new ScenarioDto())
        then:
        value == '342452258'
    }

    def "IF with AND conditions"() {
        given:
        String json = JsonFileUtil.getJsonFromFile(this.getClass(), "-multipleAndConditions.json")
        when:
        Object value = jsonLogic.calculate(json, new ScenarioDto())
        then:
        value == 'No'
    }

    def "IF with double conditions"() {
        given:
        String json = JsonFileUtil.getJsonFromFile(this.getClass(), "-doubleEqualsCondition.json")
        when:
        Object value = jsonLogic.calculate(json, new ScenarioDto())
        then:
        value == 'ноль'
    }
}
