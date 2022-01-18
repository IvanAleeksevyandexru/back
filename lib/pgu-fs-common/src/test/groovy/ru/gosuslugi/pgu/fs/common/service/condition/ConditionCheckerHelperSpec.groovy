package ru.gosuslugi.pgu.fs.common.service.condition

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import ru.gosuslugi.pgu.common.core.exception.ValidationException
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.RuleCondition
import ru.gosuslugi.pgu.dto.descriptor.types.ConditionFieldType
import ru.gosuslugi.pgu.dto.descriptor.types.PredicateArgument
import ru.gosuslugi.pgu.dto.descriptor.types.PredicateArgumentType
import ru.gosuslugi.pgu.fs.common.component.validation.CalculationPredicate
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.common.service.ProtectedFieldService
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.common.variable.TodayVariable
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry
import ru.gosuslugi.pgu.fs.common.variable.VariableType
import spock.lang.Specification

class ConditionCheckerHelperSpec extends Specification {

    StringPredicateFactory stringPredicateFactory
    IntegerPredicateFactory integerPredicateFactory
    BooleanPredicateFactory booleanPredicateFactory
    ArrayPredicateFactory arrayPredicateFactory
    DatePredicateFactory datePredicateFactory
    ApplicantAnswerPredicateFactory answerPredicateFactory
    ProtectedFieldService protectedFieldService
    VariableRegistry variableRegistry

    JsonProcessingService jsonProcessingService
    ScenarioDto scenarioDto

    ConditionCheckerHelper helper

    def setup() {
        stringPredicateFactory = new StringPredicateFactory()
        integerPredicateFactory = new IntegerPredicateFactory()
        booleanPredicateFactory = new BooleanPredicateFactory()
        datePredicateFactory = new DatePredicateFactory()
        answerPredicateFactory = new ApplicantAnswerPredicateFactory()

        protectedFieldService = Mock(ProtectedFieldService) {
            it.getValue(_ as String) >> "01.01.1991"
        }

        variableRegistry = Mock(VariableRegistry) {
            it.getVariable(VariableType.targetId) >> Mock(TodayVariable) {
                it.getValue(_ as ScenarioDto) >> "1000001"
            }
            it.getVariable(VariableType.serviceId) >> Mock(TodayVariable) {
                it.getValue(_ as ScenarioDto) >> "-1000001"
            }
            it.getVariable(VariableType.today) >> Mock(TodayVariable) {
                it.getValue(_ as ScenarioDto) >> "02.02.1992"
            }
        }

        helper = new ConditionCheckerHelper(
                stringPredicateFactory,
                integerPredicateFactory,
                booleanPredicateFactory,
                datePredicateFactory,
                arrayPredicateFactory,
                answerPredicateFactory,
                protectedFieldService,
                variableRegistry
        )

        jsonProcessingService = new JsonProcessingServiceImpl(new ObjectMapper())
        scenarioDto = configureScenarioDto()
    }

    def "Should check properly [String Predicates]"() {
        given:
        DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getApplicantAnswers()))
        RuleCondition ruleCondition = new RuleCondition(
                ConditionFieldType.String, predicate, args, "c1.value.str_value", null, null, true, null
        )

        when:
        boolean actual = helper.check(ruleCondition, [applicantAnswersContext], scenarioDto)

        then:
        actual

        where:
        predicate << ["equals", "notEquals", "matches", "regionMatches"]
        args << [[[type: PredicateArgumentType.UserConst, value: "string"] as PredicateArgument] as List<PredicateArgument>,
                 [[type: PredicateArgumentType.UserConst, value: "String"] as PredicateArgument] as List<PredicateArgument>,
                 [[type: PredicateArgumentType.UserConst, value: "[a-zA-Z]{6}"] as PredicateArgument] as List<PredicateArgument>,
                 [[type: PredicateArgumentType.UserConst, value: "super_string"] as PredicateArgument,
                  [type: PredicateArgumentType.UserConst, value: "0"] as PredicateArgument,
                  [type: PredicateArgumentType.UserConst, value: "6"] as PredicateArgument,
                  [type: PredicateArgumentType.UserConst, value: "6"] as PredicateArgument] as List<PredicateArgument>]
    }

    def "Should check properly [Integer Predicates]"() {
        given:
        DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getApplicantAnswers()))
        RuleCondition ruleCondition = new RuleCondition(
                ConditionFieldType.Integer, predicate, args, "c1.value.int_value", null, null, true, null
        )

        when:
        boolean actual = helper.check(ruleCondition, [applicantAnswersContext], scenarioDto)

        then:
        actual

        where:
        predicate << ["equals", "greaterThanOrEqualTo", "smallerThanOrEqualTo", "greaterThan", "smallerThan"]
        args << [[[type: PredicateArgumentType.UserConst, value: 47] as PredicateArgument] as List<PredicateArgument>,
                 [[type: PredicateArgumentType.UserConst, value: 47] as PredicateArgument] as List<PredicateArgument>,
                 [[type: PredicateArgumentType.UserConst, value: 47] as PredicateArgument] as List<PredicateArgument>,
                 [[type: PredicateArgumentType.UserConst, value: 46] as PredicateArgument] as List<PredicateArgument>,
                 [[type: PredicateArgumentType.UserConst, value: 48] as PredicateArgument] as List<PredicateArgument>]
    }

    def "Should check properly [Boolean Predicates]"() {
        given:
        DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getApplicantAnswers()))
        RuleCondition ruleCondition = new RuleCondition(
                ConditionFieldType.Boolean, predicate, args, "c1.value.bool_value", null, null, true, null
        )

        when:
        boolean actual = helper.check(ruleCondition, [applicantAnswersContext], scenarioDto)

        then:
        actual == expected

        where:
        predicate << ["isTrue", "isFalse"]
        args << [[[type: PredicateArgumentType.UserConst, value: true] as PredicateArgument] as List<PredicateArgument>,
                 [[type: PredicateArgumentType.UserConst, value: false] as PredicateArgument] as List<PredicateArgument>]
        expected << [true, false]
    }

    def "Should check properly [Date Predicates]"() {
        given:
        DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getApplicantAnswers()))
        RuleCondition ruleCondition = new RuleCondition(
                ConditionFieldType.Date, predicate, args, null, null, "today", true, null
        )

        when:
        boolean actual = helper.check(ruleCondition, [applicantAnswersContext], scenarioDto)

        then:
        actual == expected

        where:
        predicate << ["before", "after", "equals"]
        args << [[[type: PredicateArgumentType.ProtectedField, value: "birthDate"] as PredicateArgument,
                  [type: PredicateArgumentType.UserConst, value: "day"] as PredicateArgument,
                  [type: PredicateArgumentType.UserConst, value: "{\"year\":0,\"month\":0,\"day\":0}"] as PredicateArgument] as List<PredicateArgument>,
                 [[type: PredicateArgumentType.ProtectedField, value: "birthDate"] as PredicateArgument] as List<PredicateArgument>,
                 [[type: PredicateArgumentType.ProtectedField, value: "birthDate"] as PredicateArgument] as List<PredicateArgument>]
        expected << [false, true, false]
    }

    def "Should check properly [ApplicationAnswer Predicates]"() {
        given:
        DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getApplicantAnswers()))
        RuleCondition ruleCondition = new RuleCondition(
                ConditionFieldType.ApplicantAnswer, predicate, null, field, null, null, null, null
        )

        when:
        boolean actual = helper.check(ruleCondition, [applicantAnswersContext], scenarioDto)

        then:
        actual

        where:
        predicate << ["isVisited", "notVisited"]
        field << ["c2", "c3"]
    }

    def "Should check properly [isNull Predicate]"() {
        given:
        DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getApplicantAnswers()))
        RuleCondition ruleCondition = new RuleCondition(
                fieldType, "isNull", null, field, null, null, null, null
        )

        when:
        boolean actual = helper.check(ruleCondition, [applicantAnswersContext], scenarioDto)

        then:
        actual == expected

        where:
        field << ["c2", "c3", "fake_answer", "c1.value"]
        fieldType << [null, null, null, ConditionFieldType.String]
        expected << [false, false, true, false]
    }

    def "Should check properly [nonNull Predicate]"() {
        given:
        DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getApplicantAnswers()))
        RuleCondition ruleCondition = new RuleCondition(
                fieldType, "nonNull", null, field, null, null, null, null
        )

        when:
        boolean actual = helper.check(ruleCondition, [applicantAnswersContext], scenarioDto)

        then:
        actual == expected

        where:
        field << ["c2", "c3", "fake_answer", "c1.value"]
        fieldType << [null, null, null, ConditionFieldType.String]
        expected << [true, true, false, true]
    }

    def "Check should return false when field is null"() {
        given:
        DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getApplicantAnswers()))
        RuleCondition ruleCondition = new RuleCondition(
                fieldType, "equals", null, "fake_answer", null, null, null, null
        )

        when:
        boolean actual = helper.check(ruleCondition, [applicantAnswersContext], scenarioDto)

        then:
        !actual

        where:
        fieldType << [ConditionFieldType.String, ConditionFieldType.Integer, ConditionFieldType.Boolean, ConditionFieldType.Date]
    }

    def "Should return false for complex field type"() {
        given:
        DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getApplicantAnswers()))
        RuleCondition ruleCondition = new RuleCondition(
                ConditionFieldType.String, "equals", null, "c3", null, null, null, null
        )

        when:
        boolean actual = helper.check(ruleCondition, [applicantAnswersContext], scenarioDto)

        then:
        !actual
    }

    def "Should get first from context"() {
        when:
        DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getApplicantAnswers()))
        def actual = helper.getFirstFromContexts(field, List.of(applicantAnswersContext), clazz)

        then:
        actual == expected

        where:
        field                 | clazz                 || expected
        "c1.value.str_value"  | String.class          || 'string'
        "c1.value.int_value"  | Integer.class         || 47
        "c1.value.bool_value" | Boolean.class         || true
        "c2"                  | ApplicantAnswer.class || [visited: true, value: '{\"some_value\"}'] as ApplicantAnswer
    }

    def "Should return predicate arguments [util]"() {
        given:
        List<PredicateArgument> predicateArguments = [predicateArgument]
        DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getApplicantAnswers()))

        when:
        def predicateArgs = helper.getPredicateArgs(predicateArguments, [applicantAnswersContext], scenarioDto)

        then:
        notThrown(ValidationException)
        predicateArgs.first() == expected

        where:
        predicateArgument                                                                           || expected
        [value: "user_const", type: PredicateArgumentType.UserConst] as PredicateArgument           || 'user_const'
        [value: "c1.value.req_data", type: PredicateArgumentType.RequestData] as PredicateArgument  || 'resp_data'
        [value: "protected_field", type: PredicateArgumentType.ProtectedField] as PredicateArgument || '01.01.1991'
        [value: "targetId", type: PredicateArgumentType.Variable] as PredicateArgument              || '1000001'
        [value: "serviceId", type: PredicateArgumentType.Variable] as PredicateArgument             || '-1000001'
        [value: "today", type: PredicateArgumentType.Variable] as PredicateArgument                 || '02.02.1992'
    }

    static def configureScenarioDto() {
        def dto = new ScenarioDto()
        dto.setApplicantAnswers(Map.of(
                "c1", [visited: true, value: '{"str_value":"string","int_value":47,"bool_value":true,"req_data":"resp_data"}'] as ApplicantAnswer,
                "c2", [visited: true, value: '{\"some_value\"}'] as ApplicantAnswer,
                "c3", [visited: false, value: '{\"some_value\"}'] as ApplicantAnswer
        ))
        return dto
    }
}
