package ru.gosuslugi.pgu.fs.common.service.condition;

import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.fs.common.exception.ConditionCheckerException;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Component
public class ApplicantAnswerPredicateFactory implements PredicateFactory<ApplicantAnswer> {

    @Override
    public Predicate<ApplicantAnswer> getPredicateByName(String predicateName, List<String> args) {
        switch (ApplicantAnswerTypePredicate.valueOf(predicateName)) {
            case isVisited:
                return i -> Objects.nonNull(i) && i.getVisited();
            case notVisited:
                return i -> Objects.isNull(i) || !i.getVisited();
            default:
                throw new ConditionCheckerException("Condition predicate with name " + predicateName + " for type ApplicantAnswer not supported");
        }
    }

    @Override
    public int getArgumentNumberForPredicate(String predicateName) {
        return ApplicantAnswerTypePredicate.valueOf(predicateName).getExpectedNumberOfArguments();
    }

    private enum ApplicantAnswerTypePredicate {
        isVisited(0),
        notVisited(0);

        @Getter
        private final int expectedNumberOfArguments;

        ApplicantAnswerTypePredicate(int expectedNumberOfArguments) {
            this.expectedNumberOfArguments = expectedNumberOfArguments;
        }
    }
}
