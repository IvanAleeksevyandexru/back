package ru.gosuslugi.pgu.fs.common.jsonlogic.operator;

import ru.gosuslugi.pgu.fs.common.jsonlogic.exception.JsonLogicEvaluationException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CatOperator implements PreEvaluatedArgumentsOperator {
    public static final CatOperator CAT = new CatOperator();

    @Override
    public String key() {
        return "cat";
    }

    @Override
    public Object evaluate(List arguments) throws JsonLogicEvaluationException {
        List<String> strs = ((List<Object>) arguments).stream().map(Objects::toString).collect(Collectors.toList());
        return String.join("", strs);
    }
}
