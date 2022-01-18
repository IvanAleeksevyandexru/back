package ru.gosuslugi.pgu.kafkacli.props

import spock.lang.Specification

class IncludeExcludePropertiesTest extends Specification {
    def "includes"() {

        when:
        def p = new IncludeExcludeProperties<String>(include: include, exclude: exclude)

        then:
        p.includes(value) == includes

        where:
        include     | exclude     | value | includes
        Set.of("A") | Set.of("B") | "A"   | true
        Set.of("A") | Set.of("B") | "B"   | false
        Set.of("A") | Set.of("B") | "C"   | false
        Set.of()    | Set.of("B") | "C"   | true
        Set.of()    | Set.of("B") | "B"   | false
        Set.of("A") | Set.of()    | "A"   | true
        Set.of("A") | Set.of()    | "B"   | false

    }
}
