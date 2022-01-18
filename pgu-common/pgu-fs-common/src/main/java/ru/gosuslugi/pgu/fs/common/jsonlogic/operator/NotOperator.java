package ru.gosuslugi.pgu.fs.common.jsonlogic.operator;

import ru.gosuslugi.pgu.fs.common.jsonlogic.JsonLogic;
import ru.gosuslugi.pgu.fs.common.jsonlogic.exception.JsonLogicEvaluationException;

import java.util.List;

public class NotOperator implements PreEvaluatedArgumentsOperator {
    public static final NotOperator NOT = new NotOperator();

    @Override
    public String key() {
        return "not";
    }

    @Override
    public Object evaluate(List arguments) throws JsonLogicEvaluationException {
        if (arguments.size() > 1) {
            throw new JsonLogicEvaluationException("Оператор NOT ожидает 1 аргумент");
        }
        if (arguments.isEmpty()) {
            return false;
        }
        return !JsonLogic.isTrue(arguments.get(0));
    }
}
