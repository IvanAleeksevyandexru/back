package ru.gosuslugi.pgu.fs.common.service.condition

import ru.gosuslugi.pgu.fs.common.exception.ConditionCheckerException
import spock.lang.Specification

class BooleanPredicateFactorySpec extends Specification {

    PredicateFactory<Boolean> predicateFactory

    def setup() {
        predicateFactory = new BooleanPredicateFactory()
    }

    def "Should return valid argument number for predicate"() {
        when:
        def actual = predicateFactory.getArgumentNumberForPredicate(predicateName)

        then:
        actual == expected

        where:
        predicateName || expected
        "isTrue"      || 0
        "isFalse"     || 0
    }

    def "Should return correct predicate by name"() {
        when:
        def actual = predicateFactory.getPredicateByName(predicateName, [])

        then:
        actual.test(arg) == expected

        where:
        predicateName | arg  || expected
        "isTrue"      | true || true
        "isFalse"     | true || false
    }

    def "Should return correct predicate"() {
        when:
        def actual = predicateFactory.getPredicate(predicateName, [])

        then:
        notThrown(ConditionCheckerException)
        actual.test(arg) == expected

        where:
        predicateName | arg  || expected
        "isTrue"      | true || true
        "isFalse"     | true || false
    }
}
