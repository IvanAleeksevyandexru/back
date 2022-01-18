package ru.gosuslugi.pgu.fs.common.service.condition

import ru.gosuslugi.pgu.fs.common.exception.ConditionCheckerException
import spock.lang.Specification

class IntegerPredicateFactorySpec extends Specification {

    PredicateFactory<Integer> predicateFactory

    def setup() {
        predicateFactory = new IntegerPredicateFactory()
    }

    def "Should return valid argument number for predicate"() {
        when:
        def actual = predicateFactory.getArgumentNumberForPredicate(predicateName)

        then:
        actual == expected

        where:
        predicateName          || expected
        "equals"               || 1
        "greaterThanOrEqualTo" || 1
        "smallerThanOrEqualTo" || 1
        "greaterThan"          || 1
        "smallerThan"          || 1
    }

    def "Should return correct predicate by name"() {
        when:
        def actual = predicateFactory.getPredicateByName(predicateName, ["1"])

        then:
        actual.test(arg) == expected

        where:
        predicateName          | arg || expected
        "equals"               | 1   || true
        "greaterThanOrEqualTo" | 1   || true
        "smallerThanOrEqualTo" | 1   || true
        "greaterThan"          | 1   || false
        "smallerThan"          | 1   || false
    }

    def "Should return correct predicate"() {
        when:
        def actual = predicateFactory.getPredicate(predicateName, ["1"])

        then:
        notThrown(ConditionCheckerException)
        actual.test(arg) == expected

        where:
        predicateName          | arg || expected
        "equals"               | 1   || true
        "greaterThanOrEqualTo" | 1   || true
        "smallerThanOrEqualTo" | 1   || true
        "greaterThan"          | 1   || false
        "smallerThan"          | 1   || false
    }
}
