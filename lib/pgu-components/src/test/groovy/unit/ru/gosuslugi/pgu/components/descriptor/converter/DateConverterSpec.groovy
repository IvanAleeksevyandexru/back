package unit.ru.gosuslugi.pgu.components.descriptor.converter

import ru.gosuslugi.pgu.components.descriptor.converter.DateConverter
import spock.lang.Specification

class DateConverterSpec extends Specification {

    def converter = new DateConverter()

    def 'Can convert date value by formatter'() {
        given:
        def result

        when:
        result = converter.convert(value, attrs as Map<String, Object>)

        then:
        result == expectedResult

        where:
        value                              | attrs                                                              | expectedResult
        '2021-09-01T00:00:00.000+12:00'    | Collections.emptyMap()                                             | '2021-09-01T00:00:00.000+12:00'
        '2021-09-01T00:00:00.000+12:00'    | Map.of("format", "dd.MM.yyyy", "timezone", "+03:00")               | '01.09.2021'
        '2021-09-01T00:00:00.000+12:00'    | Map.of("format", "yyyy.MM.dd HH:mm", "timezone", "+03:00")         | '2021.08.31 15:00'
        null                               | Collections.emptyMap()                                             | ''
        ''                                 | Collections.emptyMap()                                             | ''
    }

}