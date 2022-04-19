package ru.gosuslugi.pgu.fs.common.service.condition

import ru.gosuslugi.pgu.fs.common.exception.ConditionCheckerException
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import spock.lang.Specification

class ApplicantAnswerPredicateFactorySpec extends Specification {

    PredicateFactory<ApplicantAnswer> predicateFactory

    def setup() {
        predicateFactory = new ApplicantAnswerPredicateFactory()
    }

    def "Should return valid argument number for predicate"() {
        when:
        def actual = predicateFactory.getArgumentNumberForPredicate(predicateName)

        then:
        actual == expected

        where:
        predicateName || expected
        "isVisited"   || 0
        "notVisited"  || 0
    }

    def "Should return correct predicate by name"() {
        when:
        def actual = predicateFactory.getPredicateByName(predicateName, [])

        then:
        actual.test(arg) == expected

        where:
        predicateName | arg                                 || expected
        "isVisited"   | [visited: true] as ApplicantAnswer  || true
        "notVisited"  | [visited: false] as ApplicantAnswer || true
        "isVisited"   | null                                || false
        "notVisited"  | null                                || true
    }

    def "Should return correct predicate"() {
        when:
        def actual = predicateFactory.getPredicate(predicateName, [])

        then:
        notThrown(ConditionCheckerException)
        actual.test(arg) == expected

        where:
        predicateName | arg                                 || expected
        "isVisited"   | [visited: true] as ApplicantAnswer  || true
        "notVisited"  | [visited: false] as ApplicantAnswer || true
        "isVisited"   | null                                || false
        "notVisited"  | null                                || true
    }
}
