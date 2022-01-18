package ru.gosuslugi.pgu.fs.common.jsonlogic.operator;

import ru.gosuslugi.pgu.fs.common.jsonlogic.exception.JsonLogicEvaluationException;

import java.util.List;

public class SubstrOperator implements PreEvaluatedArgumentsOperator {
    public static final SubstrOperator SUBSTR = new SubstrOperator();

    @Override
    public String key() {
        return "substr";
    }

    @Override
    public Object evaluate(List arguments) throws JsonLogicEvaluationException {
        checkArguments(arguments);
        String str = String.valueOf(arguments.get(0));
        int beginIndex = 0;
        int endIndex = str.length();
        if (arguments.size() == 2) {
            beginIndex = ((Double) arguments.get(1)).intValue();
            if (beginIndex < 0) {
                beginIndex = str.length() + beginIndex;
            }
        }
        if (arguments.size() == 3) {
            int thirdArg = ((Double) arguments.get(2)).intValue();
            beginIndex = ((Double) arguments.get(1)).intValue();
            endIndex = beginIndex + thirdArg;
            if (thirdArg < 0) {
                endIndex = str.length() + thirdArg;
            }
        }

        if (beginIndex < 0) {
            beginIndex = 0;
        }
        if (beginIndex > str.length()) {
            beginIndex = str.length();
        }
        if (endIndex < 0) {
            endIndex = 0;
        }
        if (endIndex > str.length()) {
            endIndex = str.length();
        }
        if (endIndex < beginIndex) {
            endIndex = beginIndex;
        }
        return str.substring(beginIndex, endIndex);
    }

    private void checkArguments(List arguments) {
        if (arguments.size() != 2 && arguments.size() != 3) throw new JsonLogicEvaluationException("Оператор substr ожидает 2 или 3 аргумента");
        for (int i = 1; i < arguments.size(); i++) {
            if (!(arguments.get(i) instanceof Number)) throw new JsonLogicEvaluationException("Оператор substr ожидает " + (i+1) + " аргумент типа Number");
        }
        if (!(arguments.get(0) instanceof String)) throw new JsonLogicEvaluationException("Оператор substr ожидает 1 аргумент типа String");
        if (arguments.size() == 3 && (Double) arguments.get(1) < 0) throw new JsonLogicEvaluationException("Оператор substr, 2 аргумент не может быть отрицательным");
    }
}
