package ru.gosuslugi.pgu.fs.common.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.fs.common.service.EvaluationExpressionService;
import ru.gosuslugi.pgu.fs.common.service.ReplacerContext;
import ru.gosuslugi.pgu.fs.common.utils.ReplacerWrapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationExpressionServiceImpl implements EvaluationExpressionService {
    private final static String NOT_CORRECT_CONDITION_PARSING = "Выражение для валидации '%s' задано некорректно";
    private final static String NOT_CORRECT_CONDITION_EVALUATION = "Выражение для валидации '%s' не может быть вычислено";

    @Override
    public String evaluateExpression(String expressionStr, ExpressionParser expressionParser, EvaluationContext context) {
        try {
            log.debug("Start expression evaluation {}", expressionStr);
            Expression springExpression = expressionParser.parseExpression(expressionStr);
            String expressionValue = Optional.ofNullable(springExpression.getValue(context)).orElse("").toString();
            log.debug("Expression evaluation result is {} for expression {}", expressionValue, expressionStr);
            return expressionValue;
        } catch (ParseException e) {
            throw new FormBaseException(String.format(NOT_CORRECT_CONDITION_PARSING, expressionStr), e);
        } catch (EvaluationException e) {
            throw new FormBaseException(String.format(NOT_CORRECT_CONDITION_EVALUATION, expressionStr), e);
        }
    }

    @Override
    public String replaceExpressionVariables(String expressionStr, ReplacerContext replacerContext, ReplacerWrapper wrapper) {
        String expression = expressionStr;
        Map<String, String> replacementValues = new LinkedHashMap<>();
        Matcher expressionMatcher = wrapper.getPattern().matcher(expression);
        while (expressionMatcher.find()) {
            String variableExpression = expressionMatcher.group(1); // выражение со ссылками на значения полей внутри враппера
            Object object = replacerContext.replace(variableExpression);
            String varValue = object == null ? "" : object.toString();
            // spel затем конвертирует "" в "
            varValue = varValue.replace("\"\"", "\"\"\"\"");
            replacementValues.put(wrapper.wrap(variableExpression), varValue);
        }
        log.debug("{} variables were found in expression {}", replacementValues.size(), expressionStr);
        if (!replacementValues.isEmpty()) {
            expression = StringUtils.replaceEach(expression, replacementValues.keySet().toArray(new String[0]), replacementValues.values().toArray(new String[0]));
        }
        return expression;
    }
}
