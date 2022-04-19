package ru.gosuslugi.pgu.fs.common.jsonlogic.operator;

import ru.gosuslugi.pgu.fs.common.jsonlogic.Evaluator;
import ru.gosuslugi.pgu.fs.common.jsonlogic.exception.JsonLogicEvaluationException;
import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.ArrayNode;
import ru.gosuslugi.pgu.fs.common.jsonlogic.utils.ArrayLike;

import java.util.List;

public interface PreEvaluatedArgumentsOperator extends Operator {
    Object evaluate(List arguments) throws JsonLogicEvaluationException;

    default Object evaluate(Evaluator evaluator, ArrayNode arguments) throws JsonLogicEvaluationException {
        List<Object> values = evaluator.evaluate(arguments);
        if (values.size() == 1 && ArrayLike.isEligible(values.get(0))) {
            values = new ArrayLike(values.get(0));
        }
        return evaluate(values);
    }
}
