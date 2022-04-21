package ru.gosuslugi.pgu.fs.common.jsonlogic.operator;

import lombok.RequiredArgsConstructor;
import ru.gosuslugi.pgu.fs.common.jsonlogic.Evaluator;
import ru.gosuslugi.pgu.fs.common.jsonlogic.JsonLogic;
import ru.gosuslugi.pgu.fs.common.jsonlogic.exception.JsonLogicEvaluationException;
import ru.gosuslugi.pgu.fs.common.jsonlogic.exception.JsonLogicException;
import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.ArrayNode;
import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.Node;

@RequiredArgsConstructor
public class LogicOperator implements Operator {

    public static final LogicOperator AND = new LogicOperator(true);
    public static final LogicOperator OR = new LogicOperator(false);

    private final boolean isAnd;

    @Override
    public String key() {
        return isAnd ? "and" : "or";
    }

    @Override
    public Object evaluate(Evaluator evaluator, ArrayNode arguments) throws JsonLogicException {
        if (arguments.size() < 1) {
            throw new JsonLogicEvaluationException("Логический оператор ожидает минимум 1 аргумент");
        }
        Object result = null;
        for (Node node : arguments) {
            result = evaluator.evaluate(node);
            if ((isAnd && !JsonLogic.isTrue(result)) || (!isAnd && JsonLogic.isTrue(result))) {
                return result;
            }
        }
        return result;

    }
}
