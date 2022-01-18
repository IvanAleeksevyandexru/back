package unit.ru.gosuslugi.pgu.components.descriptor.converter


import ru.gosuslugi.pgu.components.descriptor.converter.UnixDateConverter
import spock.lang.Specification

class UnixDateConverterSpec extends Specification {

    def converter = new UnixDateConverter()

    def 'Can convert unix-date value by formatter'() {
        given:
        def result

        when:
        result = converter.convert(value, attrs as Map<String, Object>)

        then:
        result == expectedResult

        where:
        value           | attrs                                                              | expectedResult
        '1617812104'    | Collections.emptyMap()                                                  | '07.04.2021'
        '1617812104'    | Map.of("format", "dd-MM-yyyy")                                          | '07-04-2021'
        '1617812104'    | Map.of("format", "yyyy-MM-dd")                                          | '2021-04-07'
        null            | Collections.emptyMap()                                                  | ''
        ''              | Collections.emptyMap()                                                  | ''
    }

}