package ru.gosuslugi.pgu.fs.common.jsonlogic.operator

import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.fs.common.jsonlogic.JsonLogicSpec

class NotEmptyOperatorTest extends JsonLogicSpec {

    def "protected argument without value"() {
        when:
        Object value = jsonLogic.calculate("{\"notEmpty\" : [\"protected.omsNumber\"]}", new ScenarioDto())
        then:
        !value
    }

    def "answer argument without value"() {
        when:
        Object value = jsonLogic.calculate("{\"notEmpty\" : [\"answer.cs1\"]}", new ScenarioDto())
        then:
        !value
    }

    def "variable argument without value"() {
        when:
        Object value = jsonLogic.calculate("{\"notEmpty\" : [\"variable.userRegionCode\"]}", new ScenarioDto())
        then:
        !value
    }

    def "Simple argument without value"() {
        when:
        Object value = jsonLogic.calculate("{\"notEmpty\" : [\"cs1.value\"]}", new ScenarioDto())
        then:
        value
    }

}