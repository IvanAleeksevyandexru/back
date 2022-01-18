package unit.ru.gosuslugi.pgu.components.descriptor.converter

import ru.gosuslugi.pgu.components.descriptor.converter.ToUnixDateConverter
import spock.lang.Specification

class ToUnixDateConverterSpec extends Specification {

    def converter = new ToUnixDateConverter()

    def 'Can convert date value by formatter to unix-date'() {
        given:
        def result

        when:
        result = converter.convert(value, attrs as Map<String, Object>)

        then:
        result == expectedResult

        where:
        expectedResult           | attrs                                                          | value
        '1617812104'    | Collections.emptyMap()                                                  | '2021-04-07T16:15:04.000'
        '1617812104'    | Map.of("format", "dd-MM-yyyy HH:mm:ss")                                 | '07-04-2021 16:15:04'
        '1617753600'    | Collections.emptyMap()                                                  | '2021-04-07'
        ''              | Collections.emptyMap()                                                  | null
        ''              | Collections.emptyMap()                                                  | ''
    }
}