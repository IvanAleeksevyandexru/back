package ru.gosuslugi.pgu.fs.common.service.condition;

import ru.gosuslugi.pgu.fs.common.exception.ConditionCheckerException;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public interface PredicateFactory<T> {

    /**
     * Provide predicate by predicate name
     * @param predicateName predicate name(e.g "equals")
     * @param args arguments for predicate
     * @return {@link Predicate} by predicate name
     */
    default Predicate<T> getPredicate(String predicateName, List<String> args) {
        checkArgumentsNumber(args, predicateName);
        return getPredicateByName(predicateName, args);
    }

    default void checkArgumentsNumber(List<String> args, String predicateName) {
        int minArgsNumber = getArgumentNumberForPredicate(predicateName);
        if ((Objects.isNull(args) && minArgsNumber != 0) || (Objects.nonNull(args) && args.size() < minArgsNumber)){
            throw new ConditionCheckerException(String.format("Number of arguments %d is not equal expected %d for predicate %s", Objects.isNull(args) ? 0 : args.size(), minArgsNumber, predicateName));
        }
    }

    Predicate<T> getPredicateByName(String predicateName, List<String> args);

    int getArgumentNumberForPredicate(String predicateName);
}
