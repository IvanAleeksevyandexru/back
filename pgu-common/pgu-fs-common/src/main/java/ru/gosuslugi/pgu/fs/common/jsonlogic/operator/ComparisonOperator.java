package ru.gosuslugi.pgu.fs.common.jsonlogic.operator;

import lombok.RequiredArgsConstructor;
import ru.gosuslugi.pgu.common.core.date.util.DateUtil;
import ru.gosuslugi.pgu.fs.common.jsonlogic.JsonLogic;
import ru.gosuslugi.pgu.fs.common.jsonlogic.exception.JsonLogicEvaluationException;
import ru.gosuslugi.pgu.fs.common.jsonlogic.exception.JsonLogicParseException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class ComparisonOperator implements PreEvaluatedArgumentsOperator {

    public static final ComparisonOperator GT = new ComparisonOperator(">", c -> c > 0);
    public static final ComparisonOperator GTE = new ComparisonOperator(">=", c -> c >= 0);
    public static final ComparisonOperator LT = new ComparisonOperator("<", c -> c < 0);
    public static final ComparisonOperator LTE = new ComparisonOperator("<=", c -> c <= 0);
    public static final ComparisonOperator EQUALS = new ComparisonOperator("==", c -> c == 0);
    public static final ComparisonOperator NOT_EQUALS = new ComparisonOperator("!=", c -> c != 0);

    private final String key;
    private final Predicate<Integer> comparePredicate;

    @Override
    public String key() {
        return key;
    }

    @Override
    public Object evaluate(List arguments) {
        if (arguments.size() != 2) {
            throw new JsonLogicEvaluationException("Оператор сравнения ожидает 2 аргумента");
        }

        Object left = arguments.get(0);
        Object right = arguments.get(1);

        int comparisonResult = Boolean.compare(JsonLogic.isTrue(left), JsonLogic.isTrue(right));

        try {
            if (left == null || right == null) {
                comparisonResult = left == right ? 0 : -1;
            }

            if (left instanceof Number && right instanceof Number) {
                comparisonResult = Double.compare(((Number) left).doubleValue(), ((Number) right).doubleValue());
            }

            if (left instanceof Number && right instanceof String) {
                comparisonResult = compareNumberToString((Number) left, (String) right);
            }

            if (left instanceof Number && right instanceof Boolean) {
                comparisonResult = compareNumberToBoolean((Number) left, (Boolean) right);
            }

            if (left instanceof String && right instanceof String) {
                comparisonResult = ((String) left).compareTo((String) right);
            }

            if (left instanceof String && right instanceof Number) {
                comparisonResult = -compareNumberToString((Number) right, (String) left);
            }

            if (left instanceof String && right instanceof Boolean) {
                comparisonResult = compareStringToBoolean((String) left, (Boolean) right);
            }

            if (left instanceof String && right instanceof OffsetDateTime) {
                comparisonResult = compareStringToDate((String) left, (OffsetDateTime) right);
            }

            if (left instanceof Boolean && right instanceof Boolean) {
                comparisonResult = Boolean.compare((Boolean) left, (Boolean) right);
            }

            if (left instanceof Boolean && right instanceof Number) {
                comparisonResult = -compareNumberToBoolean((Number) right, (Boolean) left);
            }

            if (left instanceof Boolean && right instanceof String) {
                comparisonResult = -compareStringToBoolean((String) right, (Boolean) left);
            }

            if (left instanceof OffsetDateTime && right instanceof OffsetDateTime) {
                comparisonResult = ((OffsetDateTime) left).compareTo((OffsetDateTime) right);
            }

            if (left instanceof OffsetDateTime && right instanceof String) {
                comparisonResult = -compareStringToDate((String) right, (OffsetDateTime) left);
            }
        } catch (JsonLogicParseException e) {
            return false;
        }

        return comparePredicate.test(comparisonResult);
    }

    private int compareStringToDate(String left, OffsetDateTime right) {
        OffsetDateTime leftDate = DateUtil.parseDate(left, "day");
        return leftDate.compareTo(right);
    }

    private int compareNumberToString(Number left, String right) {
        try {
            double rightDouble = right.isBlank() ? Double.NaN : Double.parseDouble(right);
            return Double.compare(left.doubleValue(), rightDouble);
        } catch (NumberFormatException e) {
            throw new JsonLogicParseException(String.format("Ошибка парсинга числа: %s", right));
        }
    }

    private int compareNumberToBoolean(Number left, Boolean right) {
        if (right) {
            return Double.compare(left.doubleValue(), 1.0);
        }
        return Double.compare(left.doubleValue(), 0.0);
    }

    private int compareStringToBoolean(String left, Boolean right) {
        return Boolean.compare(JsonLogic.isTrue(left), right);
    }

}
