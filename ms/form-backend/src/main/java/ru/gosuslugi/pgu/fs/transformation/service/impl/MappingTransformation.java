package ru.gosuslugi.pgu.fs.transformation.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.draft.model.DraftHolderDto;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.descriptor.transformation.TransformationOperation;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.service.RuleConditionService;
import ru.gosuslugi.pgu.fs.transformation.AnswerTransformationRule;
import ru.gosuslugi.pgu.fs.transformation.Transformation;
import ru.gosuslugi.pgu.fs.transformation.TransformationResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@AllArgsConstructor
public class MappingTransformation implements Transformation {

    public static final String TRANSFORMATION_RULES_ATTR_NAME = "transformationRules";

    private final RuleConditionService ruleConditionService;
    private final JsonProcessingService jsonProcessingService;

    @Override
    public TransformationOperation getOperation() {
        return TransformationOperation.mappingTransformation;
    }

    @Override
    public TransformationResult transform(DraftHolderDto draftOrigin, Order orderOrigin, Map<String, Object> spec) {
        Map<String, ApplicantAnswer> originAnswers = draftOrigin.getBody().getApplicantAnswers();
        DocumentContext applicantAnswersContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(originAnswers));
        Map<String, ApplicantAnswer> mappedApplicantAnswers = new HashMap<>();
        draftOrigin.getBody().setApplicantAnswers(mappedApplicantAnswers);


        Map<String, List<AnswerTransformationRule>> answersTransformationsRules = JsonProcessingUtil.getObjectMapper().convertValue(spec.get(TRANSFORMATION_RULES_ATTR_NAME), new TypeReference<>() {});
        if (Objects.nonNull(answersTransformationsRules)) {
            for (Map.Entry<String, List<AnswerTransformationRule>> answerTransformation: answersTransformationsRules.entrySet()) {
                answerTransformation.getValue().forEach(rule -> {
                    if ((Objects.isNull(rule.getConditions()) || rule.getConditions().isEmpty()) || ruleConditionService.isRuleApplyToAnswers(rule.getConditions(), List.of(originAnswers), List.of(applicantAnswersContext), draftOrigin.getBody())) {
                        ApplicantAnswer applicantAnswer = new ApplicantAnswer();
                        applicantAnswer.setVisited(true);
                        applicantAnswer.setValue(JsonProcessingUtil.toJson(getValueForRule(rule.getTransformation(), applicantAnswersContext)));
                        mappedApplicantAnswers.put(answerTransformation.getKey(), applicantAnswer);
                    }
                });
            }
        }
        return new TransformationResult(true, draftOrigin, orderOrigin);
    }

    private Object getValueForRule(Object rule, DocumentContext applicantAnswersContext) {
        if (Objects.nonNull(rule) && StringUtils.hasText(rule.toString())) {
            if (rule instanceof Map) {
                Map<String, Object> result = new HashMap<>();
                ((Map<String, Object>) rule).entrySet().forEach(entry -> result.put(entry.getKey(), getValueForRule(entry.getValue(), applicantAnswersContext)));
                return result;
            }
            if (rule instanceof String) {
                String ruleValue = (String) rule;
                Object result = ruleValue;
                if (ruleValue.startsWith("$")) {
                    try {
                        result = applicantAnswersContext.read(ruleValue.replace("$", "$."));
                    } catch (PathNotFoundException e) {
                        result = null;
                    }
                }
                return result;
            }
        }
        return null;
    }

}
