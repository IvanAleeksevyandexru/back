package ru.gosuslugi.pgu.fs.common.service.impl;


import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.LinkedValue;
import ru.gosuslugi.pgu.dto.descriptor.types.ValueDefinition;
import ru.gosuslugi.pgu.fs.common.service.functions.ExpressionMethods;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;

@Slf4j
@Component
public class DefinitionResolver {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([a-zA-Z0-9_.?@&|!><'()=:\\-\\[\\]\\s]+)}");
    private static final String DEF_EMPTY_VALUE = "EMPTY_VALUE";
    private static final String EMPTY = "empty";

    private ExpressionParser parser;
    private ExpressionMethods expressionMethods;

    public DefinitionResolver() {
        expressionMethods = new ExpressionMethods();
        parser = new SpelExpressionParser();
    }

    public Object compute(LinkedValue linkedValue, ScenarioDto scenarioDto) {
        ValueDefinition definition = linkedValue.getDefinition();

        List<String> args = definition.getArgs();
        List<String> expressions = definition.getExpressions();

        SimpleEvaluationContext evaluationContext = SimpleEvaluationContext
                .forReadOnlyDataBinding()
                .withInstanceMethods().build();

        addServiceVariables(evaluationContext);
        resolveArguments(scenarioDto, definition, args, evaluationContext);

        return computeResult(expressions, evaluationContext);
    }

    private void resolveArguments(ScenarioDto scenarioDto, ValueDefinition definition, List<String> args, SimpleEvaluationContext evaluationContext) {
        for (int i = 0; i < definition.getArgs().size(); i++) {
            if (StringUtils.isEmpty(args.get(i))) return;
            String resolved = resolvePlaceholders(args.get(i), scenarioDto);
            Expression expression = parser.parseExpression(resolved);
            evaluationContext.setVariable("arg" + (i + 1), expression.getValue(evaluationContext, expressionMethods));
        }
    }

    private Object computeResult(List<String> expressions, SimpleEvaluationContext evaluationContext) {
        for (String expression : expressions) {
            if (StringUtils.isEmpty(expression)) return DEF_EMPTY_VALUE;
            String mapped = addMetaSymbolToArgs(expression);
            Expression ex = parser.parseExpression(mapped);
            Object resultComputation = ex.getValue(evaluationContext, expressionMethods);
            boolean isOptional = resultComputation instanceof Optional;
            if (!isOptional) return resultComputation;
        }
        return DEF_EMPTY_VALUE;
    }

    private void addServiceVariables(SimpleEvaluationContext evaluationContext) {
        evaluationContext.setVariable("today", LocalDate.now());
    }

    private String resolvePlaceholders(String value, ScenarioDto scenarioDto) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(value);
        List<PlaceholderHolder> holders = new ArrayList<>();

        while (matcher.find()) holders.add(PlaceholderHolder.of(matcher.group(0), matcher.group(1)));
        if (holders.isEmpty()) return value;

        String result = null;
        for (PlaceholderHolder holder : holders) {
            result = Objects.requireNonNullElse(result, value)
                    .replace(holder.getWrapped(), getAnswerValue(scenarioDto, holder.getValue()));
        }
        return result;
    }

    private String addMetaSymbolToArgs(String originalExpression) {
        String replacedString = StringUtils.replace(originalExpression, "arg", "#arg");
        replacedString = StringUtils.replace(replacedString, "today", "#today");

        return replacedString;
    }

    private String getAnswerValue(ScenarioDto scenarioDto, String path) {
        int pointIndex = path.indexOf(".");
        if (pointIndex < 0) {
            ApplicantAnswer applicantAnswer = scenarioDto.getApplicantAnswers().get(path);
            return isNull(applicantAnswer) ? EMPTY : applicantAnswer.getValue();
        }
        String appAnswerComponentId = path.substring(0, pointIndex);
        String jsonPath = path.substring(pointIndex + 1);

        ApplicantAnswer applicantAnswer = scenarioDto.getApplicantAnswers().get(appAnswerComponentId);
        if (isNull(applicantAnswer)) return EMPTY;

        String answerValue = applicantAnswer.getValue();
        try {
            return Objects.toString(JsonPath.read(answerValue, "$." + jsonPath));
        } catch (PathNotFoundException ex) {
            return EMPTY;
        }
    }
}
