package ru.gosuslugi.pgu.fs.common.service.condition

import ru.gosuslugi.pgu.fs.common.exception.ConditionCheckerException
import spock.lang.Specification

class ArrayPredicateFactorySpec extends Specification {

    PredicateFactory<List<Object>> predicateFactory

    def setup() {
        predicateFactory = new ArrayPredicateFactory()
    }

    def "Should return valid argument number for predicate"() {
        when:
        def actual = predicateFactory.getArgumentNumberForPredicate(predicateName)

        then:
        actual == expected

        where:
        predicateName || expected
        "containsOne" || 1
        "containsAll" || 1
        "notContains" || 1
    }

    def "Test predicate 'containsOne'"() {
        when:
        def actual = predicateFactory.getPredicate('containsOne', jsonArgs)

        then:
        actual.test(contextValues) == expected

        where:
        contextValues          | jsonArgs          || expected
        ["one", "two", "tree"] | ["one"]           || true
        ["one", "two", "tree"] | ["two", "ignore"] || true
        ["one", "two", "tree"] | ["four"]          || false
        ["one", "two", null]   | [null]            || true
        ["one", "two", "tree"] | [null]            || false
        [1, 2, 3]              | ["1"]             || true
        [1, 2, 3]              | ["1", "4"]        || true
        [1, 2, 3]              | ["4"]             || false
        [true, false]          | ["true"]          || true
        [true, false]          | [null]            || false
        [true, false, null]    | [null]            || true
    }

    def "Test predicate 'containsAll'"() {
        when:
        def actual = predicateFactory.getPredicate('containsAll', jsonArgs)

        then:
        actual.test(contextValues) == expected

        where:
        contextValues          | jsonArgs          || expected
        ["one", "two", "tree"] | ["one", "two"]           || true
        ["one", "two", "tree"] | ["two", "no"] || false
        ["one", "two", "tree"] | ["four"]          || false
        ["one", "two", null]   | [null]            || true
        ["one", "two", "tree"] | [null]            || false
        [1, 2, 3]              | ["2", "1"]             || true
        [1, 2, 3]              | ["1", "4"]        || false
        [1, 2, 3]              | ["4"]             || false
        [true, false]          | ["true", "false"]          || true
        [true, false]          | [null]            || false
        [true, false, null]    | [null]            || true
    }


    def "Test predicate 'notContains'"() {
        when:
        def actual = predicateFactory.getPredicate('notContains', jsonArgs)

        then:
        actual.test(contextValues) == expected

        where:
        contextValues          | jsonArgs          || expected
        ["one", "two", "tree"] | ["one"]           || false
        ["one", "two", "tree"] | ["four", "one"] || true
        ["one", "two", "tree"] | ["four"]          || true
        ["one", "two", null]   | [null]            || false
        ["one", "two", "tree"] | [null]            || true
        [1, 2, 3]              | ["1"]             || false
        [1, 2, 3]              | ["4"]             || true
        [true, false]          | ["true"]          || false
        [true, false]          | [null]            || true
        [true, false, null]    | [null]            || false
    }

    def "Should throw ConditionCheckerException"() {
        when:
        predicateFactory.getPredicate(predicateName, [])

        then:
        thrown(ConditionCheckerException)

        where:
        predicateName | _
        "containsOne" | _
        "containsAll" | _
        "notContains" | _
    }
}
