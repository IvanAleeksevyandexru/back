package ru.gosuslugi.pgu.fs.common.service.condition;

import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.fs.common.exception.ConditionCheckerException;

import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

@Component
public class ArrayPredicateFactory implements PredicateFactory<List<Object>> {

    @Override
    public Predicate<List<Object>> getPredicateByName(String predicateName, List<String> args) {
        switch (ArrayTypePredicate.valueOf(predicateName)) {
            case containsOne:
                return arr -> toListOfStrings(arr).contains(args.get(0));
            case containsAll:
                return arr -> toListOfStrings(arr).containsAll(args);
            case notContains:
                return arr -> !toListOfStrings(arr).contains(args.get(0));
            default:
                throw new ConditionCheckerException("Condition predicate with name " + predicateName + " for type String not supported");
        }
    }

    private List<String> toListOfStrings(List<Object> source) {
        return source.stream()
                .map(item -> item == null ? null : item.toString())
                .collect(toList());
    }

    @Override
    public int getArgumentNumberForPredicate(String predicateName) {
        return ArrayTypePredicate.valueOf(predicateName).getExpectedNumberOfArguments();
    }

    private enum ArrayTypePredicate {
        containsAll(1),
        containsOne(1),
        notContains(1);

        @Getter
        private final int expectedNumberOfArguments;

        ArrayTypePredicate(int expectedNumberOfArguments) {
            this.expectedNumberOfArguments = expectedNumberOfArguments;
        }
    }
}
