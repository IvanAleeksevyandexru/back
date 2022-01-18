package unit.ru.gosuslugi.pgu.components.descriptor.converter

import ru.gosuslugi.pgu.components.descriptor.converter.StringReplaceConverter
import spock.lang.Shared
import spock.lang.Specification

class StringReplaceConverterSpec extends Specification {

    StringReplaceConverter converter
    @Shared String value

    def setup() {
        converter = new StringReplaceConverter()
    }

    def setupSpec() {
        value = "Например, пенсия будет рассчитана следующим образом: 125,403 балла * 98, 86 руб. + 6 044,48 руб. (фиксированная выплата); Размер пенсии составит: 18 441,82 руб."
    }

    def "When replacement regexp is't exists"() {
        given:
        Map<String, Object> converterSettings = new HashMap() {{
            put("converter", "REPLACE");
            put("forisabsent", null);
        }};

        when:
        def result = converter.convert(value, converterSettings)

        then:
        result == value
    }

    def "When replacement regexp is null"() {
        given:
        Map<String, Object> converterSettings = new HashMap() {{
            put("converter", "REPLACE");
            put("for", null);
        }};

        when:
        def result = converter.convert(value, converterSettings)

        then:
        result == value
    }

    def "When replacement regexp is empty"() {
        given:
        Map<String, Object> converterSettings = new HashMap() {{
            put("converter", "REPLACE");
            put("for", "");
        }};

        when:
        def result = converter.convert(value, converterSettings)

        then:
        result == value
    }

    def 'When single regexp'() {
        given:
        Map<String, Object> converterSettings = new HashMap() {{
            put("converter", "REPLACE");
            put("for", of("руб.", "₽"));
        }};

        when:
        def result = converter.convert(value, converterSettings)

        then:
        result == "Например, пенсия будет рассчитана следующим образом: 125,403 балла * 98, 86 ₽ + 6 044,48 ₽ (фиксированная выплата); Размер пенсии составит: 18 441,82 ₽"
    }
}
