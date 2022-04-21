package ru.gosuslugi.pgu.fs.common.service.condition

import ru.gosuslugi.pgu.fs.common.exception.ConditionCheckerException
import spock.lang.Specification

class DatePredicateFactorySpec extends Specification {

    PredicateFactory<String> predicateFactory

    def setup() {
        predicateFactory = new DatePredicateFactory()
    }

    def "Should return valid argument number for predicate"() {
        when:
        def actual = predicateFactory.getArgumentNumberForPredicate(predicateName)

        then:
        actual == expected

        where:
        predicateName || expected
        "before"      || 1
        "after"       || 1
        "equals"      || 1
    }

    def "Should return correct predicate by name"() {
        when:
        def actual = predicateFactory.getPredicateByName(predicateName, target + accuracy + addToDate)

        then:
        actual.test(source) == expected

        where:
        predicateName | source                | target                  | accuracy   | addToDate                                                                         || expected
        "before"      | "2001"                | ["2002"]                | ["year"]   | []                                                                                || true
        "after"       | "2001-01"             | ["2002-02"]             | ["month"]  | []                                                                                || false
        "equals"      | "2001-01-01"          | ["2002-02-02"]          | ["day"]    | []                                                                                || false

        "before"      | "2001-01-01 01"       | ["2002-02-02 02"]       | ["hour"]   | []                                                                                || true
        "after"       | "2001-01-01 01:01"    | ["2002-02-02 02:02"]    | ["minute"] | []                                                                                || false
        "equals"      | "2001-01-01 01:01:01" | ["2002-02-02 02:02:02"] | ["second"] | []                                                                                || false

        "before"      | "2001"                | ["2002"]                | ["year"]   | ["{\"year\":-1,\"month\":-1,\"day\":0,\"hour\":0,\"minute\":0,\"second\":0}"]     || false
        "after"       | "2001-01"             | ["2002-02"]             | ["month"]  | ["{\"year\":-1,\"month\":-1,\"day\":-1,\"hour\":0,\"minute\":0,\"second\":0}"]    || true
        "equals"      | "2001-01-01"          | ["2002-02-02"]          | ["day"]    | ["{\"year\":-1,\"month\":-1,\"day\":-1,\"hour\":0,\"minute\":0,\"second\":0}"]    || true

        "before"      | "2001-01-01 01"       | ["2002-02-02 02"]       | ["hour"]   | ["{\"year\":-1,\"month\":-1,\"day\":-1,\"hour\":-1,\"minute\":-1,\"second\":0}"]  || false
        "after"       | "2001-01-01 01:01"    | ["2002-02-02 02:02"]    | ["minute"] | ["{\"year\":-1,\"month\":-1,\"day\":-1,\"hour\":-1,\"minute\":-1,\"second\":-1}"] || true
        "equals"      | "2001-01-01 01:01:01" | ["2002-02-02 02:02:02"] | ["second"] | ["{\"year\":-1,\"month\":-1,\"day\":-1,\"hour\":-1,\"minute\":-1,\"second\":-1}"] || true

        "before"      | "01.01.2001"          | ["02.02.2002"]          | []         | []                                                                                || true
        "after"       | "01.01.2001"          | ["02.02.2002"]          | []         | []                                                                                || false
        "equals"      | "01.01.2001"          | ["02.02.2002"]          | []         | []                                                                                || false
    }

    def "Should return correct predicate"() {
        when:
        def actual = predicateFactory.getPredicate(predicateName, target + accuracy + addToDate)

        then:
        notThrown(ConditionCheckerException)
        actual.test(source) == expected

        where:
        predicateName | source                | target                  | accuracy   | addToDate                                                                         || expected
        "before"      | "2001"                | ["2002"]                | ["year"]   | []                                                                                || true
        "after"       | "2001-01"             | ["2002-02"]             | ["month"]  | []                                                                                || false
        "equals"      | "2001-01-01"          | ["2002-02-02"]          | ["day"]    | []                                                                                || false

        "before"      | "2001-01-01 01"       | ["2002-02-02 02"]       | ["hour"]   | []                                                                                || true
        "after"       | "2001-01-01 01:01"    | ["2002-02-02 02:02"]    | ["minute"] | []                                                                                || false
        "equals"      | "2001-01-01 01:01:01" | ["2002-02-02 02:02:02"] | ["second"] | []                                                                                || false

        "before"      | "2001"                | ["2002"]                | ["year"]   | ["{\"year\":-1,\"month\":-1,\"day\":0,\"hour\":0,\"minute\":0,\"second\":0}"]     || false
        "after"       | "2001-01"             | ["2002-02"]             | ["month"]  | ["{\"year\":-1,\"month\":-1,\"day\":-1,\"hour\":0,\"minute\":0,\"second\":0}"]    || true
        "equals"      | "2001-01-01"          | ["2002-02-02"]          | ["day"]    | ["{\"year\":-1,\"month\":-1,\"day\":-1,\"hour\":0,\"minute\":0,\"second\":0}"]    || true

        "before"      | "2001-01-01 01"       | ["2002-02-02 02"]       | ["hour"]   | ["{\"year\":-1,\"month\":-1,\"day\":-1,\"hour\":-1,\"minute\":-1,\"second\":0}"]  || false
        "after"       | "2001-01-01 01:01"    | ["2002-02-02 02:02"]    | ["minute"] | ["{\"year\":-1,\"month\":-1,\"day\":-1,\"hour\":-1,\"minute\":-1,\"second\":-1}"] || true
        "equals"      | "2001-01-01 01:01:01" | ["2002-02-02 02:02:02"] | ["second"] | ["{\"year\":-1,\"month\":-1,\"day\":-1,\"hour\":-1,\"minute\":-1,\"second\":-1}"] || true

        "before"      | "01.01.2001"          | ["02.02.2002"]          | []         | []                                                                                || true
        "after"       | "01.01.2001"          | ["02.02.2002"]          | []         | []                                                                                || false
        "equals"      | "01.01.2001"          | ["02.02.2002"]          | []         | []                                                                                || false
    }
}
