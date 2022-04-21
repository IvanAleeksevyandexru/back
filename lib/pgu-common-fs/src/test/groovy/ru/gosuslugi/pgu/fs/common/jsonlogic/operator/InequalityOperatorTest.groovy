package ru.gosuslugi.pgu.fs.common.jsonlogic.operator

import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.fs.common.jsonlogic.JsonLogicSpec


\
class InequalityOperatorTest extends JsonLogicSpec {

    def "Different value same type"() {
        when:
        Object value = jsonLogic.calculate("{\"!=\" : [1, 2]}", new ScenarioDto())
        then:
        value == true
    }

    def "Same value different type"() {
        when:
        Object value = jsonLogic.calculate("{\"!=\" : [1, \"1.0\"]}", new ScenarioDto())
        then:
        value == false
    }

    def "Different value different type"() {
        when:
        Object value = jsonLogic.calculate("{\"!=\" : [[], false]}", new ScenarioDto())
        then:
        value == false
    }

    def "Empty string and true"() {
        when:
        Object value = jsonLogic.calculate("{\"!=\" : [\" \", true]}", new ScenarioDto())
        then:
        value == true
    }

    def "String and date not equals"() {
        given:
        String json = JsonFileUtil.getJsonFromFile(this.getClass(), "-stringAndDate.json")
        when:
        Object value = jsonLogic.calculate(json, new ScenarioDto())
        then:
        value == true
    }

    def "Two dates not equals"() {
        given:
        String json = JsonFileUtil.getJsonFromFile(this.getClass(), "-twoDates.json")
        when:
        Object value = jsonLogic.calculate(json, new ScenarioDto())
        then:
        value == true
    }
}
