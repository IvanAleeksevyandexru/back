package ru.gosuslugi.pgu.fs.common.service.condition;

import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.fs.common.exception.ConditionCheckerException;

import java.util.List;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;

@Component
public class StringPredicateFactory implements PredicateFactory<String> {

    @Override
    public Predicate<String> getPredicateByName(String predicateName, List<String> args) {
        switch (StringTypePredicate.valueOf(predicateName)) {
            case equals:
                return s -> s.equals(args.get(0));
            case notEquals:
                return s -> !s.equals(args.get(0));
            case containsInList:
                return args::contains;
            case notContainsInList:
                return not(args::contains);
            case matches:
                return s -> s.matches(args.get(0));
            case nonMatches:
                return s -> !s.matches(args.get(0));
            case regionMatches:
                return s -> s.regionMatches(Integer.parseInt(args.get(1)), args.get(0), Integer.parseInt(args.get(2)), Integer.parseInt(args.get(3)));
            case regionNonMatches:
                return s -> !s.regionMatches(Integer.parseInt(args.get(1)), args.get(0), Integer.parseInt(args.get(2)), Integer.parseInt(args.get(3)));
            case greaterThanOrEqualTo:
                return s -> s.compareTo(args.get(0)) >= 0;
            case smallerThanOrEqualTo:
                return s -> s.compareTo(args.get(0)) <= 0;
            case greaterThan:
                return s -> s.compareTo(args.get(0)) > 0;
            case smallerThan:
                return s -> s.compareTo(args.get(0)) < 0;
            case equalsIgnoreCase:
                return s-> s.equalsIgnoreCase(args.get(0));
            case notEqualsIgnoreCase:
                return s-> !s.equalsIgnoreCase(args.get(0));
            default:
                throw new ConditionCheckerException("Condition predicate with name " + predicateName + " for type String not supported");
        }
    }

    @Override
    public int getArgumentNumberForPredicate(String predicateName) {
        return StringTypePredicate.valueOf(predicateName).getExpectedNumberOfArguments();
    }

    private enum StringTypePredicate {
        equals(1),
        notEquals(1),
        containsInList(2),      // Проверяет, что строковое значение содержится в переданном в json списке строк
        notContainsInList(2),
        matches(1),
        nonMatches(1),
        regionMatches(4),
        regionNonMatches(4),
        greaterThanOrEqualTo(1),
        smallerThanOrEqualTo(1),
        greaterThan(1),
        smallerThan(1),
        equalsIgnoreCase(1),
        notEqualsIgnoreCase(1);

        @Getter
        private final int expectedNumberOfArguments;

        StringTypePredicate(int expectedNumberOfArguments) {
            this.expectedNumberOfArguments = expectedNumberOfArguments;
        }
    }
}
