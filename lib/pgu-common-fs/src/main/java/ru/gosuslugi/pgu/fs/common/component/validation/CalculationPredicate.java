package ru.gosuslugi.pgu.fs.common.component.validation;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.components.ValidationUtil;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.service.EvaluationExpressionService;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.service.condition.ConditionCheckerHelper;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.common.utils.ReplacerWrapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Предикат с условиями на компоненты из {@link ScenarioDto#getCurrentValue()} */
@Component
@Slf4j
public class CalculationPredicate implements ValidationRule {

    private final static String PREDICATE_TYPE = "CalculatedPredicate";
    private final static String EXPR_ATTR = "expr";
    private final static String ERROR_MSG_ATTR = "errorMsg";

    private final JsonProcessingService jsonProcessingService;
    private final ConditionCheckerHelper conditionCheckerHelper;
    private final EvaluationExpressionService evaluationExpressionService;
    private final ExpressionParser expressionParser;
    private final EvaluationContext context;

    public CalculationPredicate(JsonProcessingService jsonProcessingService, ConditionCheckerHelper conditionCheckerHelper, EvaluationExpressionService evaluationExpressionService) {
        this.jsonProcessingService = jsonProcessingService;
        this.conditionCheckerHelper = conditionCheckerHelper;
        this.evaluationExpressionService = evaluationExpressionService;
        this.expressionParser = new SpelExpressionParser();
        this.context = SimpleEvaluationContext.forReadOnlyDataBinding().withInstanceMethods().build();
    }

    @Override
    public Map.Entry<String, String> validate(Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent, ScenarioDto scenarioDto) {
        List<Map<Object, Object>> predicateValidations = getPredicateValidationList(fieldComponent);
        if (CollectionUtils.isEmpty(predicateValidations)) {
            return null;
        }
        String entryValue = AnswerUtil.getValue(entry);
        List<DocumentContext> contexts = getContexts(entryValue, scenarioDto.getCurrentValue());
        List<String> errors = predicateValidations.stream()
                .map(predicateMap -> getExpressionValue(contexts, predicateMap))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        if (!errors.isEmpty()) {
            return Map.entry(fieldComponent.getId(), String.join(", ", errors));
        }
        return null;
    }

    private String getExpressionValue(List<DocumentContext> contexts, Map<Object, Object> predicateMap) {
        String expression = calculateExpression((String) predicateMap.get(EXPR_ATTR), contexts);
        return Boolean.TRUE.equals(Boolean.parseBoolean(expression)) ? null : (String) predicateMap.get(ERROR_MSG_ATTR);
    }

    private List<DocumentContext> getContexts(String entryValue, Map<String, ApplicantAnswer> currentValue) {
        DocumentContext answerContext = JsonPath.parse(jsonProcessingService.toJson(entryValue));
        DocumentContext currentValueContext = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(currentValue));
        return List.of(answerContext, currentValueContext);
    }

    private List<Map<Object, Object>> getPredicateValidationList(FieldComponent fieldComponent) {
        return FieldComponentUtil.getList(fieldComponent, FieldComponentUtil.VALIDATION_ARRAY_KEY, true)
                .stream()
                .filter(
                        validationRule ->
                                PREDICATE_TYPE.equalsIgnoreCase((String) validationRule.get(ValidationUtil.VALIDATION_TYPE))
                                        && StringUtils.isNotBlank((String) validationRule.get(EXPR_ATTR))
                )
                .collect(Collectors.toList());
    }

    /**
     * Вычисляет выражения в поле {@link #EXPR_ATTR}
     * @param exprStr вычисляемое выражение с переменными из {@link ScenarioDto#getCurrentValue()} или {@link ScenarioDto#getApplicantAnswers()}
     * @param documentContexts json-контексты для поиска значения
     * @return вычисленное значение SPEL-выражения после подстановки переменных
     */
    private String calculateExpression(String exprStr, List<DocumentContext> documentContexts) {
        String expression = evaluationExpressionService.replaceExpressionVariables(exprStr,
                variableExpression -> conditionCheckerHelper.getFirstFromContexts(variableExpression, documentContexts, String.class),
                ReplacerWrapper.DOLLAR_CURLY_BRACES);
        return evaluationExpressionService.evaluateExpression(expression, expressionParser, context);
    }
}
