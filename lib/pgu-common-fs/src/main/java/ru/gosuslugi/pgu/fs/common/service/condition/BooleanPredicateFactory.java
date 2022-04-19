package ru.gosuslugi.pgu.fs.common.service.condition;

import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.fs.common.exception.ConditionCheckerException;

import java.util.List;
import java.util.function.Predicate;

@Component
public class BooleanPredicateFactory implements PredicateFactory<Boolean> {

    @Override
    public Predicate<Boolean> getPredicateByName(String predicateName, List<String> args) {
        switch (BooleanTypePredicate.valueOf(predicateName)) {
            case isTrue:
                return i -> i;
            case isFalse:
                return i -> !i;
            default:
                throw new ConditionCheckerException("Condition predicate with name " + predicateName + " for type Boolean not supported");
        }
    }

    @Override
    public int getArgumentNumberForPredicate(String predicateName) {
        return BooleanTypePredicate.valueOf(predicateName).getExpectedNumberOfArguments();
    }

    private enum BooleanTypePredicate {
        isTrue(0),
        isFalse(0);

        @Getter
        private final int expectedNumberOfArguments;

        BooleanTypePredicate(int expectedNumberOfArguments) {
            this.expectedNumberOfArguments = expectedNumberOfArguments;
        }
    }
}
