package unit.ru.gosuslugi.pgu.components.descriptor.converter

import ru.gosuslugi.pgu.components.descriptor.converter.RubCurrencyConverter
import spock.lang.Specification

class RubCurrencyConverterSpec extends Specification {

    def converter = new RubCurrencyConverter()

    def 'Convert value to rub currency'() {
        given:
        def result

        when:
        result = converter.convert(value, attrs as Map<String, Object>)

        then:
        result == expectedResult

        where:
        value            | attrs                                   | expectedResult
        null             | Collections.emptyMap()                  | ''
        ''               | Collections.emptyMap()                  | ''
        0                | Collections.emptyMap()                  | '0,00 ₽'
        '1500000'        | Collections.emptyMap()                  | '1 500 000,00 ₽'
        '1500000.038'    | Collections.emptyMap()                  | '1 500 000,04 ₽'
        '1500000.038'    | Collections.emptyMap()                  | '1 500 000,04 ₽'
        '2.8519344E7'    | Collections.emptyMap()                  | '28 519 344,00 ₽'
        '5.9849670091E8' | Collections.emptyMap()                  | '598 496 700,91 ₽'
        '5.9849670091E8' | [excludeSymbol: true]                   | '598 496 700,91'
        '5.9849670091E8' | [roundFloor: true]                      | '598 496 700 ₽'
        '5.9849670091E8' | [excludeSymbol: true, roundFloor: true] | '598 496 700'
    }

    def 'Failed convert value to rub currency'() {
        expect:
        converter.convert('fail value', Collections.emptyMap()) == 'fail value'
    }
}