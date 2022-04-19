package ru.gosuslugi.pgu.fs.common.jsonlogic

import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.fs.common.service.ProtectedFieldService
import ru.gosuslugi.pgu.fs.common.service.condition.ApplicantAnswerPredicateFactory
import ru.gosuslugi.pgu.fs.common.service.condition.ArrayPredicateFactory
import ru.gosuslugi.pgu.fs.common.service.condition.BooleanPredicateFactory
import ru.gosuslugi.pgu.fs.common.service.condition.ConditionCheckerHelper
import ru.gosuslugi.pgu.fs.common.service.condition.DatePredicateFactory
import ru.gosuslugi.pgu.fs.common.service.condition.IntegerPredicateFactory
import ru.gosuslugi.pgu.fs.common.service.condition.StringPredicateFactory
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry
import spock.lang.Specification

class JsonLogicSpec extends Specification {

    static JsonLogic jsonLogic

    def setupSpec() {
        def jsonProcessingService = new JsonProcessingServiceImpl(JsonProcessingUtil.getObjectMapper())
        def protectedFieldService = Mock(ProtectedFieldService)
        def variableRegistry = new VariableRegistry()
        def conditionCheckerHelper = new ConditionCheckerHelper(
                new StringPredicateFactory(),
                new IntegerPredicateFactory(),
                new BooleanPredicateFactory(),
                new DatePredicateFactory(),
                new ArrayPredicateFactory(),
                new ApplicantAnswerPredicateFactory(),
                protectedFieldService,
                variableRegistry
        )
        jsonLogic = new JsonLogic(new Parser(
                protectedFieldService,
                variableRegistry,
                jsonProcessingService,
                conditionCheckerHelper))
    }
}
