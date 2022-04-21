package ru.gosuslugi.pgu.fs.common.jsonlogic.operator

import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.fs.common.jsonlogic.JsonLogicSpec
import ru.gosuslugi.pgu.fs.common.jsonlogic.exception.JsonLogicEvaluationException

class NotOperatorTest extends JsonLogicSpec {

    def "Not false"() {
        when:
        Object value = jsonLogic.calculate("{\"not\" : false}", new ScenarioDto())
        then:
        value == true
    }

    def "Not false in array"() {
        when:
        Object value = jsonLogic.calculate("{\"not\" : [false]}", new ScenarioDto())
        then:
        value == true
    }

    def "When empty args return false"() {
        when:
        Object value = jsonLogic.calculate("{\"not\" : []}", new ScenarioDto())
        then:
        value == false
    }

    def "When more than one argument throw exception"() {
        when:
        Object value = jsonLogic.calculate("{\"not\" : [\" \", true]}", new ScenarioDto())
        then:
        thrown(JsonLogicEvaluationException)
    }
}
