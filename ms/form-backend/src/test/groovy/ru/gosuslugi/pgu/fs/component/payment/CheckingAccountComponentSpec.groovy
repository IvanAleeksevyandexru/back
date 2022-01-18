package ru.gosuslugi.pgu.fs.component.payment

import spock.lang.Specification

class CheckingAccountComponentSpec extends Specification {

    CheckingAccountComponent component = new CheckingAccountComponent()

    def 'Can check checking account validation'() {
        given:
        def result

        when:
        result = component.isValid(bik, checkingAccount)

        then:
        result == expectedResult

        where:
        expectedResult | bik         | checkingAccount
        false          | null        | '123'
        false          | ''          | '123'
        false          | '123'       | null
        false          | '123'       | null
        false          | '049205603' | '40817810362001249907'
        true           | '049205603' | '40817810362001249937'
    }
}