package ru.gosuslugi.pgu.fs.common.service;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import ru.gosuslugi.pgu.fs.common.utils.ReplacerWrapper;

/** Сервис для вычисления SPEL-выражений и замены переменных. */
public interface EvaluationExpressionService {

    /**
     * Вычисляет выражение заданное строкой
     * @param expression выражение, которое нужно вычислить
     * @param expressionParser парсер выражений для предварительной прекомпиляции перед вычислением
     * @param context контекст вычисления выражений
     * @return вычисленное значение в строке
     */
    String evaluateExpression(String expression, ExpressionParser expressionParser, EvaluationContext context);

    /**
     * Осуществляет замену переменных в выражении, используя указанный враппер и источник данных для замены переменных.
     * @param expressionStr выражение, в котором нужно заменить переменные
     * @param replacerContext контекст-источник данных для замены
     * @param wrapper обёртка над переменными, как они указываются в выражении
     * @return выражение-результат с заменёнными переменными
     */
    String replaceExpressionVariables(String expressionStr, ReplacerContext replacerContext, ReplacerWrapper wrapper);
}
