package ru.gosuslugi.pgu.fs.common.jsonlogic.operator;

import org.apache.commons.lang.math.NumberUtils;
import ru.gosuslugi.pgu.fs.common.jsonlogic.exception.JsonLogicEvaluationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class AdditionOperation implements PreEvaluatedArgumentsOperator {
    public static final AdditionOperation ADD = new AdditionOperation();

    @Override
    public Object evaluate(List arguments) throws JsonLogicEvaluationException {
        checkArguments(arguments);
        if (arguments.size() == 1) {
            Double result = Double.valueOf((String) arguments.get(0));
            return getResult(result);
        }
        BigDecimal result = BigDecimal.ZERO;
        for (Object arg: arguments) {
            BigDecimal argNumber = new BigDecimal(arg.toString());
            result = result.add(argNumber);
        }
        return getResult(result.doubleValue());
    }

    @Override
    public String key() {
        return "+";
    }

    private Number getResult(Double result) {
        if ((result % 1) == 0) {
            return result.longValue();
        }
        return result;
    }

    private void checkArguments(List arguments) {
        if (Objects.isNull(arguments) || arguments.isEmpty()) throw new JsonLogicEvaluationException("Оператор + ожидает 1 или более аргументов");
        if (arguments.size() == 1 && !(arguments.get(0) instanceof String)) {
            throw new JsonLogicEvaluationException("При одном аргументе оператор + ожидает тип String");
        }
        if (arguments.size() > 1) {
            if (arguments.stream().anyMatch( arg -> !(arg instanceof Number) && !NumberUtils.isNumber(arg.toString()))) throw new JsonLogicEvaluationException("Оператор + ожидает аргументы типа Number или строку-число");
        }
    }
}
