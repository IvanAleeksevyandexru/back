package ru.gosuslugi.pgu.fs.common.jsonlogic.operator

import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.fs.common.jsonlogic.JsonLogicSpec
import ru.gosuslugi.pgu.fs.common.jsonlogic.exception.JsonLogicEvaluationException

import java.util.stream.Collectors
import java.util.stream.IntStream

class AdditionOperatorTest extends JsonLogicSpec {

    def "1 argument String"() {
        when:
        Object value = jsonLogic.calculate("{\"+\" : [\"2\"]}", new ScenarioDto())
        then:
        value == 2
    }

    def "1 argument not String"() {
        when:
        jsonLogic.calculate("{\"+\" : [2]}", new ScenarioDto())
        then:
        thrown(JsonLogicEvaluationException)
    }

    def "many arguments"() {
        when:
        Object value = jsonLogic.calculate("{\"+\" : [2, -6, 1, 3]}", new ScenarioDto())
        then:
        value == 0
    }

    def "many arguments, some of them string number"() {
        when:
        Object value = jsonLogic.calculate("{\"+\" : [\"2\", -6, 1, 3]}", new ScenarioDto())
        then:
        value == 0
    }

    def "many arguments, some of them not number"() {
        when:
        jsonLogic.calculate("{\"+\" : [\"as\", -6, 1, 3]}", new ScenarioDto())
        then:
        thrown(JsonLogicEvaluationException)
    }

    def "many float arguments addition "() {
        String array = IntStream.rangeClosed(1, 100).mapToObj({ i -> i + ".25" }).collect(Collectors.joining(", ", "[", "]"))
        when:
        Object value = jsonLogic.calculate("{\"+\" : " + array + "}", new ScenarioDto())
        then:
        value == 5075
    }

    def "no arguments"() {
        when:
        jsonLogic.calculate("{\"+\" : []}", new ScenarioDto())
        then:
        thrown(JsonLogicEvaluationException)
    }

    def "many arguments, one of them double"() {
        when:
        Object value = jsonLogic.calculate("{\"+\" : [2.1, -6, 1, 3]}", new ScenarioDto())
        then:
        value instanceof Double
    }
}
