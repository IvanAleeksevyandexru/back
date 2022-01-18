package ru.gosuslugi.pgu.fs.common.service;

/**
 * Контекст для замены переменных на значения из источника, указываемого в реализации.
 * Например, источником данных может быть {@link ru.gosuslugi.pgu.dto.ScenarioDto} или {@link com.jayway.jsonpath.DocumentContext}.
 */
public interface ReplacerContext {
    /**
     * Заменяет переменную на значение из источника, указываемого в реализации.
     * @param variableExpression переменная
     * @return значение переменной
     */
    String replace(String variableExpression);
}
