package ru.gosuslugi.pgu.fs.common.jsonlogic.operator

import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.fs.common.jsonlogic.JsonLogicSpec


\
class LogicOperatorTest extends JsonLogicSpec {

    def "OR expression"() {
        when:
        Object value = jsonLogic.calculate("{\"or\" : [0, false, \"a\"]}", new ScenarioDto())
        then:
        value == "a"
    }

    def "AND expression"() {
        when:
        Object value = jsonLogic.calculate("{\"and\" : [true, \"\", 3]}", new ScenarioDto())
        then:
        value == ""
    }

}
