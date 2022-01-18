package ru.gosuslugi.pgu.fs.transformation

import com.jayway.jsonpath.DocumentContext
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.core.lk.model.order.Order
import ru.gosuslugi.pgu.draft.model.DraftHolderDto
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.RuleCondition
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.common.service.RuleConditionService
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.transformation.service.impl.MappingTransformation
import spock.lang.Shared
import spock.lang.Specification

class MappingTransformationSpec extends Specification {
    RuleConditionService ruleConditionService = Stub()
    @Shared
    JsonProcessingService jsonProcessingService = new JsonProcessingServiceImpl(JsonProcessingUtil.getObjectMapper())
    MappingTransformation mappingTransformation = new MappingTransformation(ruleConditionService, jsonProcessingService)

    static DraftHolderDto origin
    static DraftHolderDto expected
    static Order order = new Order()

    def setupSpec() {
        origin = jsonProcessingService.fromJson(
                JsonFileUtil.getJsonFromFile(this.getClass(),"_origin.json"),
                DraftHolderDto.class)
        expected = jsonProcessingService.fromJson(
                JsonFileUtil.getJsonFromFile(this.getClass(),"_expected.json"),
                DraftHolderDto.class)
    }

    def "testTransformation"() {
        given:
        Map<String, Object> map = new HashMap<>()
        Map<String, List<AnswerTransformationRule>> transformationRules = new HashMap<>()
        AnswerTransformationRule q1Rule = new AnswerTransformationRule()
        q1Rule.setTransformation('$q1.value')
        List<AnswerTransformationRule> q1Rules = List.of(q1Rule)

        AnswerTransformationRule q2Rule = new AnswerTransformationRule()
        RuleCondition q2RuleCondition = new RuleCondition()
        q2RuleCondition.setValue("q2RuleCondition")
        q2Rule.setConditions(Set.of(q2RuleCondition))
        Map<String, Object> q2Transformations = new HashMap<>()
        q2Rule.setTransformation(q2Transformations)
        q2Transformations.put("a1", "somestring")
        q2Transformations.put("b2", Map.of("c2", '$pd4.value.regAddr.fullAddress'))
        List<AnswerTransformationRule> q2Rules = List.of(q2Rule)

        AnswerTransformationRule pd3Rule = new AnswerTransformationRule()
        RuleCondition pd3RuleCondition = new RuleCondition()
        pd3RuleCondition.setValue("pd3RuleCondition")
        pd3Rule.setConditions(Set.of(pd3RuleCondition))
        pd3Rule.setTransformation('$pd3.value')
        List<AnswerTransformationRule> pd3Rules = List.of(pd3Rule)

        transformationRules.put("q1", q1Rules)
        transformationRules.put("q2", q2Rules)
        transformationRules.put("pd3", pd3Rules)
        map.put("transformationRules", transformationRules)

        ruleConditionService.isRuleApplyToAnswers(_ as Set, _ as List, _ as List, _ as ScenarioDto) >> {
            Set<RuleCondition> conditions,
            List<Map<String, ApplicantAnswer>> answersMaps,
            List<DocumentContext> documentContexts,
            ScenarioDto scenarioDto
                -> conditions.stream().anyMatch {condition -> condition.getValue() == "q2RuleCondition"}
        }

        when:
        TransformationResult result = mappingTransformation.transform(origin, order, map)

        then:
        result.isTransformed()
        expected == result.getDraftHolderDto()
    }

    def "testNoTransformationRules"() {
        given:
        Map<String, Object> map = new HashMap<>()
        map.put("transformationRules", new HashMap<>())

        when:
        TransformationResult result = mappingTransformation.transform(origin, order, map)

        then:
        result.isTransformed()
        result.getDraftHolderDto().getBody().getApplicantAnswers().isEmpty()
    }

    def "testEmptySpec"() {
        given:
        Map<String, Object> map = new HashMap<>()

        when:
        TransformationResult result = mappingTransformation.transform(origin, order, map)

        then:
        result.isTransformed()
        result.getDraftHolderDto().getBody().getApplicantAnswers().isEmpty()
    }
}
