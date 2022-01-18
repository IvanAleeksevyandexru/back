package ru.gosuslugi.pgu.fs.common.jsonlogic;

import ru.gosuslugi.pgu.common.core.date.util.DateUtil;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.argument.complex.DateArgument;
import ru.gosuslugi.pgu.fs.common.jsonlogic.exception.JsonLogicEvaluationException;
import ru.gosuslugi.pgu.fs.common.jsonlogic.operator.Operator;
import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.*;
import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.argument.complex.Complex;
import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.argument.complex.DateToStringArgument;
import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.argument.primitive.NumberArgument;
import ru.gosuslugi.pgu.fs.common.jsonlogic.structure.argument.primitive.Primitive;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Evaluator {

    private final Map<String, Operator> expressions = new HashMap<>();

    public Evaluator(Collection<Operator> operators) {
        for(Operator operator : operators) {
            this.expressions.put(operator.key(), operator);
        }
    }

    public Object evaluate(Node node) throws JsonLogicEvaluationException {
        switch (node.getType()) {
            case PRIMITIVE_ARG: return evaluate((Primitive) node);
            case COMPLEX_ARG: return evaluate((Complex) node);
            case ARRAY: return evaluate((ArrayNode)node);
            default: return evaluate((OperationNode) node);
        }
    }

    public static Object transform(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return value;
    }

    private Object evaluate(Primitive<?> primitive) {
        switch (primitive.getPrimitiveType()) {
            case NUMBER: return ((NumberArgument) primitive).getValue();
            default: return primitive.getValue();
        }
    }

    private Object evaluate(Complex<?> complex) {
        switch (complex.getComplexType()) {
            case Date:
                return getOffsetDateTime((DateArgument) complex);
            case DateToString:
                DateToStringArgument dateToStringArgument = (DateToStringArgument) complex;
                OffsetDateTime date = getOffsetDateTime(dateToStringArgument);
                return date.format(DateTimeFormatter.ofPattern(dateToStringArgument.getFormat()));
            default: return complex.getValue();
        }
    }

    public List<Object> evaluate(ArrayNode array) throws JsonLogicEvaluationException {
        List<Object> values = new ArrayList<>(array.size());
        for (Node node : array) {
            values.add(evaluate(node));
        }
        return values;
    }

    private Object evaluate(OperationNode operation) throws JsonLogicEvaluationException {
        Operator operator = expressions.get(operation.getOperator());
        if (operator == null) {
            throw new JsonLogicEvaluationException("Неизвестная операция " + operation.getOperator());
        }
        return operator.evaluate(this, operation.getArguments());
    }

    private OffsetDateTime getOffsetDateTime(DateArgument date) {
        OffsetDateTime offsetDate = DateUtil.parseDate(date.getValue(), date.getAccuracy());
        String addToDate = JsonProcessingUtil.toJson(date.getAdd());
        return DateUtil.addToDate(addToDate, offsetDate);
    }
}
