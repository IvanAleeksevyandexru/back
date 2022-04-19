package ru.gosuslugi.pgu.fs.common.jsonlogic;

import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.fs.common.jsonlogic.operator.*;

import java.lang.reflect.Array;
import java.util.*;

@Component
public class JsonLogic {

    private final List<Operator> operators;
    private final Evaluator evaluator;
    private final Parser parser;

    public JsonLogic(Parser parser) {
        this.parser = parser;
        this.operators = new ArrayList<>();
        addOperation(IfOperator.IF);
        addOperation(ComparisonOperator.GT);
        addOperation(ComparisonOperator.GTE);
        addOperation(ComparisonOperator.LT);
        addOperation(ComparisonOperator.LTE);
        addOperation(ComparisonOperator.EQUALS);
        addOperation(ComparisonOperator.NOT_EQUALS);
        addOperation(LogicOperator.AND);
        addOperation(LogicOperator.OR);
        addOperation(NotOperator.NOT);
        addOperation(NotEmptyOperator.NOT_EMPTY);
        addOperation(SubstrOperator.SUBSTR);
        addOperation(CatOperator.CAT);
        addOperation(AdditionOperation.ADD);
        addOperation(SubtractionOperation.SUB);
        this.evaluator = new Evaluator(operators);
    }

    public Object calculate(String json, ScenarioDto scenarioDto) {
        return evaluator.evaluate(parser.parse(json, scenarioDto));
    }

    public Object calculate(Object json, ScenarioDto scenarioDto) {
        String jsonString = JsonProcessingUtil.toJson(json);
        return this.calculate(jsonString, scenarioDto);
    }

    public static boolean isTrue(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (boolean) value;
        }
        if (value instanceof Number) {
            if (value instanceof Double) {
                Double d = (Double) value;
                if (d.isNaN()) {
                    return false;
                }
                if (d.isInfinite()) {
                    return true;
                }
            }
            if (value instanceof Float) {
                Float f = (Float) value;
                if (f.isNaN()) {
                    return false;
                }
                if (f.isInfinite()) {
                    return true;
                }
            }
            return ((Number)value).doubleValue() != 0.0;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean(String.valueOf(value));
        }
        if (value instanceof Collection) {
            return !((Collection)value).isEmpty();
        }
        if (value.getClass().isArray()) {
            return Array.getLength(value) > 0;
        }
        return true;
    }

    private void addOperation(Operator operator) {
        operators.add(operator);
    }
}
