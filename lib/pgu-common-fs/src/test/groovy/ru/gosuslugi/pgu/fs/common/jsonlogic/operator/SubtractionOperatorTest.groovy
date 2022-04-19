package ru.gosuslugi.pgu.fs.common.jsonlogic.operator

import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.fs.common.jsonlogic.JsonLogicSpec
import ru.gosuslugi.pgu.fs.common.jsonlogic.exception.JsonLogicEvaluationException

class SubtractionOperatorTest extends JsonLogicSpec {

    def "1 argument String"() {
        when:
        Object value = jsonLogic.calculate("{\"-\" : [2]}", new ScenarioDto())
        then:
        value == -2
    }

    def "2 arguments"() {
        when:
        Object value = jsonLogic.calculate("{\"-\" : [2, -6]}", new ScenarioDto())
        then:
        value == 8
    }

    def "2 arguments, some of them string number"() {
        when:
        Object value = jsonLogic.calculate("{\"-\" : [\"2\", -6]}", new ScenarioDto())
        then:
        value == 8
    }

    def "2 arguments, some of them not number"() {
        when:
        jsonLogic.calculate("{\"-\" : [\"as\", -6]}", new ScenarioDto())
        then:
        thrown(JsonLogicEvaluationException)
    }

    def "no arguments"() {
        when:
        jsonLogic.calculate("{\"-\" : []}", new ScenarioDto())
        then:
        thrown(JsonLogicEvaluationException)
    }

    def "2 arguments, one of them double"() {
        when:
        Object value = jsonLogic.calculate("{\"-\" : [2.1, -6]}", new ScenarioDto())
        then:
        value instanceof Double
    }
}
