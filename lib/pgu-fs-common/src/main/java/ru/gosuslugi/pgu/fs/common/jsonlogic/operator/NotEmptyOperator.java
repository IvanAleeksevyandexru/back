package ru.gosuslugi.pgu.fs.common.jsonlogic.operator;

import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.fs.common.jsonlogic.exception.JsonLogicEvaluationException;

import java.util.List;
import java.util.Objects;

public class NotEmptyOperator implements PreEvaluatedArgumentsOperator {
    public static final NotEmptyOperator NOT_EMPTY = new NotEmptyOperator();

    @Override
    public String key() {
        return "notEmpty";
    }

    @Override
    public Object evaluate(List arguments) throws JsonLogicEvaluationException {
        if (arguments.size() != 1) throw new JsonLogicEvaluationException("Оператор Not empty ожидает 1 аргумент");
        if (StringUtils.isEmpty(arguments.get(0))) {
            return false;
        }
        if (arguments.get(0).toString().startsWith("answer.")){
            return false;
        }
        return true;
    }
}
