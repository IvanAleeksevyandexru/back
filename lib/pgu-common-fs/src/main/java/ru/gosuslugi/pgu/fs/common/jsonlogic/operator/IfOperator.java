package ru.gosuslugi.pgu.fs.common.jsonlogic.operator;

import ru.gosuslugi.pgu.fs.common.jsonlogic.Evaluator;
import ru.gosuslugi.pgu.fs.common.jsonlogic.JsonLogic;
import ru.gosuslugi.pgu.fs.common.jsonlogic.exception.JsonLogicEvaluationException;
import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.ArrayNode;
import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.Node;

public class IfOperator implements Operator {

    public static final IfOperator IF = new IfOperator("if");

    private final String operator;

    private IfOperator(String operator) {
        this.operator = operator;
    }

    @Override
    public String key() {
        return operator;
    }

    @Override
    public Object evaluate(Evaluator evaluator, ArrayNode arguments) throws JsonLogicEvaluationException {
        if(arguments.size() < 1) {
            return null;
        }
        if (arguments.size() == 1) {
            return evaluator.evaluate(arguments.get(0));
        }

        if (arguments.size() == 2) {
            return JsonLogic.isTrue(evaluator.evaluate(arguments.get(0)))
                    ? evaluator.evaluate(arguments.get(1))
                    : null;

        }

        for (int i = 0; i < arguments.size() - 1; i += 2) {
            Node condition = arguments.get(i);
            Node resultIfConditionTrue = arguments.get(i + 1);
            if (JsonLogic.isTrue(evaluator.evaluate(condition))) {
                return evaluator.evaluate(resultIfConditionTrue);
            }
        }

        if ((arguments.size() & 1) == 0) {
            return null;
        }

        return evaluator.evaluate(arguments.get(arguments.size() -1));
    }
}
