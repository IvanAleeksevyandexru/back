package ru.gosuslugi.pgu.fs.common.service.condition

import ru.gosuslugi.pgu.fs.common.exception.ConditionCheckerException
import spock.lang.Specification

class StringPredicateFactorySpec extends Specification {

    PredicateFactory<String> predicateFactory

    def setup() {
        predicateFactory = new StringPredicateFactory()
    }

    def "Should return valid argument number for predicate"() {
        when:
        def actual = predicateFactory.getArgumentNumberForPredicate(predicateName)

        then:
        actual == expected

        where:
        predicateName           || expected
        "equals"                || 1
        "notEquals"             || 1
        "matches"               || 1
        "regionMatches"         || 4
        "regionNonMatches"      || 4
        "greaterThanOrEqualTo"  || 1
        "smallerThanOrEqualTo"  || 1
        "greaterThan"           || 1
        "smallerThan"           || 1
        "containsInList"        || 2
        "notContainsInList"     || 2
        "equalsIgnoreCase"      || 1
        "notEqualsIgnoreCase"   || 1
    }

    def "Should return correct predicate by name"() {
        when:
        def actual = predicateFactory.getPredicateByName(predicateName, ["test_string", "0", "5", "5"])

        then:
        actual.test(arg) == expected

        where:
        predicateName           | arg               || expected
        "equals"                | "string"          || false
        "equals"                | "test_string"     || true
        "notEquals"             | "string"          || true
        "matches"               | "string"          || false
        "regionMatches"         | "string"          || true
        "regionNonMatches"      | "test_string"     || true
        "regionNonMatches"      | "string"          || false

        "greaterThanOrEqualTo"  | "test_string"     || true
        "greaterThanOrEqualTo"  | "test_string1"    || true
        "greaterThanOrEqualTo"  | "test_strin"      || false
        "greaterThanOrEqualTo"  | "test_strinh"     || true
        "greaterThanOrEqualTo"  | "test_strinf"     || false

        "smallerThanOrEqualTo"  | "test_string"     || true
        "smallerThanOrEqualTo"  | "test_string1"    || false
        "smallerThanOrEqualTo"  | "test_strin"      || true
        "smallerThanOrEqualTo"  | "test_strinh"     || false
        "smallerThanOrEqualTo"  | "test_strinf"     || true

        "greaterThan"           | "test_string"     || false
        "greaterThan"           | "test_string1"    || true
        "greaterThan"           | "test_strin"      || false
        "greaterThan"           | "test_strinh"     || true
        "greaterThan"           | "test_strinf"     || false

        "smallerThan"           | "test_string"     || false
        "smallerThan"           | "test_string1"    || false
        "smallerThan"           | "test_strin"      || true
        "smallerThan"           | "test_strinh"     || false
        "smallerThan"           | "test_strinf"     || true

        "containsInList"        | "5"               || true
        "containsInList"        | "test_string"     || true
        "containsInList"        | "-"               || false

        "notContainsInList"     | "-"               || true
        "notContainsInList"     | "test_string"     || false
        "notContainsInList"     | "5"               || false
        "notContainsInList"     | null              || true

        "equalsIgnoreCase"      | "TEST_STRING"     || true
        "equalsIgnoreCase"      | "TEST_STRIN"      || false
        "equalsIgnoreCase"      | "test_string"     || true

        "notEqualsIgnoreCase"   | "TEST_STRING"     || false
        "notEqualsIgnoreCase"   | "TEST_STRIN"      || true
        "notEqualsIgnoreCase"   | "test_string"     || false
    }

    def "Should return correct predicate"() {
        when:
        def actual = predicateFactory.getPredicate(predicateName, ["test_string", "0", "5", "5"])

        then:
        notThrown(ConditionCheckerException)
        actual.test(arg) == expected

        where:
        predicateName           | arg               || expected
        "equals"                | "string"          || false
        "equals"                | "test_string"     || true
        "notEquals"             | "string"          || true
        "matches"               | "string"          || false
        "regionMatches"         | "string"          || true

        "greaterThanOrEqualTo"  | "test_string"     || true
        "greaterThanOrEqualTo"  | "test_string1"    || true
        "greaterThanOrEqualTo"  | "test_strin"      || false
        "greaterThanOrEqualTo"  | "test_strinh"     || true
        "greaterThanOrEqualTo"  | "test_strinf"     || false

        "smallerThanOrEqualTo"  | "test_string"     || true
        "smallerThanOrEqualTo"  | "test_string1"    || false
        "smallerThanOrEqualTo"  | "test_strin"      || true
        "smallerThanOrEqualTo"  | "test_strinh"     || false
        "smallerThanOrEqualTo"  | "test_strinf"     || true

        "greaterThan"           | "test_string"     || false
        "greaterThan"           | "test_string1"    || true
        "greaterThan"           | "test_strin"      || false
        "greaterThan"           | "test_strinh"     || true
        "greaterThan"           | "test_strinf"     || false

        "smallerThan"           | "test_string"     || false
        "smallerThan"           | "test_string1"    || false
        "smallerThan"           | "test_strin"      || true
        "smallerThan"           | "test_strinh"     || false
        "smallerThan"           | "test_strinf"     || true

        "containsInList"        | "5"               || true
        "containsInList"        | "test_string"     || true
        "containsInList"        | "-"               || false

        "notContainsInList"     | "-"               || true
        "notContainsInList"     | "test_string"     || false
        "notContainsInList"     | "5"               || false
        "notContainsInList"     | null              || true

        "equalsIgnoreCase"      | "TEST_STRING"     || true
        "equalsIgnoreCase"      | "TEST_STRIN"      || false
        "equalsIgnoreCase"      | "test_string"     || true

        "notEqualsIgnoreCase"   | "TEST_STRING"     || false
        "notEqualsIgnoreCase"   | "TEST_STRIN"      || true
        "notEqualsIgnoreCase"   | "test_string"     || false
    }
}
