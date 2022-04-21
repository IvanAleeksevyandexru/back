package ru.gosuslugi.pgu.fs.common.service.condition;

import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.fs.common.exception.ConditionCheckerException;
import ru.gosuslugi.pgu.common.core.date.util.DateUtil;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static org.springframework.util.StringUtils.hasText;


@Component
public class DatePredicateFactory implements PredicateFactory<String> {

    private static final String NULL_STRING_VALUE = "null";

    /**
     *
     * @param predicateName predicate name(e.g "equals")
     * @param args arguments for predicate:
     *             args(0) - Дата с которой сравнивают
     *             args(1) - Точность сравнения (year, month, day, hour, minute, second)
     *             args(2) - Добавление к arg(0) лет, месяцев, дней. Формат "{\"year\":0,\"month\":0,\"day\":-1}"
     * @return
     */
    @Override
    public Predicate<String> getPredicateByName(String predicateName, List<String> args) {
        final int VALUE_ARGS_INDEX = 0;
        final int ACCURACY_ARGS_INDEX = 1;
        final int ADD_ARGS_INDEX = 2;
        String valueToCompareWith = DateUtil.checkIfDateIsToday(args.get(VALUE_ARGS_INDEX));
        // Страшный костыль по причине ребят из умного поиска.
        // Просьба ногами не бить
        if(Objects.isNull(predicateName)
                || NULL_STRING_VALUE.equalsIgnoreCase(predicateName)
                || Objects.isNull(valueToCompareWith)
                || NULL_STRING_VALUE.equalsIgnoreCase(valueToCompareWith))
        {
            return i -> false;
        }

        String accuracy = "";
        if (args.size() > ACCURACY_ARGS_INDEX) {
            accuracy = args.get(ACCURACY_ARGS_INDEX);
        }
        OffsetDateTime dateToCompareWith = DateUtil.parseDate(valueToCompareWith, accuracy);
        if (args.size() > ADD_ARGS_INDEX) {
            dateToCompareWith = DateUtil.addToDate(args.get(ADD_ARGS_INDEX), dateToCompareWith);
        }
        OffsetDateTime dateForPredicate = dateToCompareWith;
        String accuracyForPredicate = accuracy;
        switch (DateTypePredicate.valueOf(predicateName)) {
            case before:
                return d -> hasText(d) && DateUtil.parseDate(d, accuracyForPredicate).isBefore(dateForPredicate);
            case after:
                return d -> hasText(d) && DateUtil.parseDate(d, accuracyForPredicate).isAfter(dateForPredicate);
            case equals:
                return d -> hasText(d) && DateUtil.parseDate(d, accuracyForPredicate).equals(dateForPredicate);
            default:
                throw new ConditionCheckerException("Condition predicate with name " + predicateName + " for type Date not supported");
        }
    }

    @Override
    public int getArgumentNumberForPredicate(String predicateName) {
        return DateTypePredicate.valueOf(predicateName).getExpectedNumberOfArguments();
    }

    private enum DateTypePredicate {
        before(1),
        after(1),
        equals(1);

        @Getter
        private final int expectedNumberOfArguments;

        DateTypePredicate(int expectedNumberOfArguments) {
            this.expectedNumberOfArguments = expectedNumberOfArguments;
        }
    }
}
