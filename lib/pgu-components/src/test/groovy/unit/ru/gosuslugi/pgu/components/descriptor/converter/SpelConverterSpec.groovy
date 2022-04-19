package unit.ru.gosuslugi.pgu.components.descriptor.converter

import ru.gosuslugi.pgu.components.descriptor.converter.SpelConverter
import spock.lang.Specification

class SpelConverterSpec extends Specification {

    def converter = new SpelConverter()

    def 'If expression not specified - return method value'() {
        given:
        def value = 'simple value'
        def result

        when:
        result = converter.convert(value, [:])

        then:
        result == value

        when:
        result = converter.convert(value, [expression: null])

        then:
        result == value

        when:
        result = converter.convert(value, [expression: ''])

        then:
        result == value
    }

    def 'Can convert value by expression and result type'() {
        given:
        def result

        when:
        result = converter.convert(value, [expression: expression, resultType: resultType])

        then:
        result == expectedResult

        where:
        value           | expression                                                             | resultType | expectedResult
        'Barcelona'     | 'charAt(0)'                                                            | null       | 'B'
        'Barcelona'     | 'substring(0, 3)'                                                      | null       | 'Bar'
        'Barcelona'     | 'toUpperCase()'                                                        | null       | 'BARCELONA'
        5               | '#this*2 + 3'                                                          | 'Integer'  | 13
        5               | '#this == 5'                                                           | 'Boolean'  | true
        ['a', 'b', 'a'] | 'stream().distinct().collect(T(java.util.stream.Collectors).toList())' | 'List'     | ['a', 'b']
        [a: 'b']        | 'get("a")'                                                             | null       | 'b'
        'a'             | 'T(java.lang.String).join(", ", #this, "b")'                           | null       | 'a, b'
        null            | 'T(java.util.Map).of("a", "b")'                                        | 'Map'      | [a: 'b']
    }

    def 'Can convert value by expression and result class'() {
        given:
        def result

        when:
        result = converter.convert(value, [expression: expression, resultClass: resultClass])

        then:
        result == expectedResult

        where:
        value | expression                    | resultClass   | expectedResult
        '5'   | '#this'                       | String.class  | '5'
        '5'   | '#this'                       | Integer.class | 5
        '5'   | '#this == 7'                  | Boolean.class | false
        '5'   | 'T(java.util.List).of(#this)' | List.class    | ['5']
    }

    def 'Can get object field value'() {
        given:
        def apple = new Apple('green', 200)
        def result

        when:
        result = converter.convert(apple, [expression: 'color'])

        then:
        result == apple.color

        when:
        result = converter.convert(apple, [expression: 'color.concat(" and red")'])

        then:
        result == 'green and red'

        when:
        result = converter.convert(apple, [expression: 'weight == 200', resultClass: Boolean.class])

        then:
        result == true
    }

    def 'SpelConverter should convert value using expression and reference value type'() {
        given:
        def result

        when:
        result = converter.convert(value, [expression: expression, refValueType: refValueType, resultType: resultType])

        then:
        result == expectedResult

        where:
        value                                           | expression | refValueType | resultType | expectedResult
        "[{\"ai7_6\":\"false\"},{\"ai7_6\":\"false\"}]" | 'size()'   | 'List'       | null       | '2'
        "{\"a\":\"false\"}"                             | 'get("a")' | 'Map'        | 'Boolean'  | false
    }

    class Apple {
        String color
        int weight

        Apple(String color, int weight) {
            this.color = color
            this.weight = weight
        }
    }

}