package ru.gosuslugi.pgu.fs.common.jsonlogic.operator;

import org.apache.commons.lang.math.NumberUtils;
import ru.gosuslugi.pgu.fs.common.jsonlogic.exception.JsonLogicEvaluationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class SubtractionOperation implements PreEvaluatedArgumentsOperator {
    public static final SubtractionOperation SUB = new SubtractionOperation();

    @Override
    public Object evaluate(List arguments) throws JsonLogicEvaluationException {
        checkArguments(arguments);
        double result;
        BigDecimal arg0 = new BigDecimal(arguments.get(0).toString());
        if (arguments.size() == 1) {
            result = arg0.negate().doubleValue();
        } else {
            BigDecimal arg1 = new BigDecimal(arguments.get(1).toString());
            result = arg0.subtract(arg1).doubleValue();
        }
        return getResult(result);
    }

    @Override
    public String key() {
        return "-";
    }

    private Number getResult(Double result) {
        if ((result % 1) == 0) {
            return result.longValue();
        }
        return result;
    }

    private void checkArguments(List arguments) {
        if (Objects.isNull(arguments) || arguments.isEmpty() || arguments.size() > 2) throw new JsonLogicEvaluationException("Оператор + ожидает 1 или 2 аргумента");
        if (arguments.stream().anyMatch( arg -> !(arg instanceof Number) && !NumberUtils.isNumber(arg.toString()))) throw new JsonLogicEvaluationException("Оператор - ожидает аргументы типа Number или строку-число");
    }
}
