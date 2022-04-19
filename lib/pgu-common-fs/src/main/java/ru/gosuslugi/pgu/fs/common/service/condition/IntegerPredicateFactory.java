package ru.gosuslugi.pgu.fs.common.service.condition;

import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.fs.common.exception.ConditionCheckerException;

import java.util.List;
import java.util.function.Predicate;

@Component
public class IntegerPredicateFactory implements PredicateFactory<Integer> {

    @Override
    public Predicate<Integer> getPredicateByName(String predicateName, List<String> args) {
        switch (IntegerTypePredicate.valueOf(predicateName)) {
            case equals:
                return i -> i == Integer.parseInt(args.get(0));
            case greaterThanOrEqualTo:
                return i -> i >= Integer.parseInt(args.get(0));
            case smallerThanOrEqualTo:
                return i -> i <= Integer.parseInt(args.get(0));
            case greaterThan:
                return i -> i > Integer.parseInt(args.get(0));
            case smallerThan:
                return i -> i < Integer.parseInt(args.get(0));
            default:
                throw new ConditionCheckerException("Condition predicate with name " + predicateName + " for type Integer not supported");
        }
    }

    @Override
    public int getArgumentNumberForPredicate(String predicateName) {
        return IntegerTypePredicate.valueOf(predicateName).getExpectedNumberOfArguments();
    }

    private enum IntegerTypePredicate {
        equals(1),
        greaterThanOrEqualTo(1),
        smallerThanOrEqualTo(1),
        greaterThan(1),
        smallerThan(1);

        @Getter
        private final int expectedNumberOfArguments;

        IntegerTypePredicate(int expectedNumberOfArguments) {
            this.expectedNumberOfArguments = expectedNumberOfArguments;
        }
    }
}
