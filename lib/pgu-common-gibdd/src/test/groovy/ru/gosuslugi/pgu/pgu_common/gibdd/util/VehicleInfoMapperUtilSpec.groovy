package ru.gosuslugi.pgu.pgu_common.gibdd.util

import spock.lang.Specification


class VehicleInfoMapperUtilSpec extends Specification {

    def 'Can get restriction status as text'() {
        given:
        def result

        when:
        result = VehicleInfoMapperUtil.getRestrictionStatusAsText(status)

        then:
        result == expectedResult

        where:
        status | expectedResult
        null   | ''
        ''     | ''
        '0'    | 'снято'
        '1'    | 'действует'
        '3'    | '3'
    }

    def 'Can get formatted number'() {
        given:
        def result

        when:
        result = VehicleInfoMapperUtil.getFormattedNumber(number)

        then:
        result == expectedResult

        where:
        number          | expectedResult
        null            | null
        ''              | ''
        '1'             | '1'
        '1234567890'    | '1234 567890'
        ' 1234567890'   | '1234 567890'
        '1234***567890' | '1234***567890'
    }

    def 'Can convert string to latin'() {
        given:
        def result

        when:
        result = VehicleInfoMapperUtil.convertToLatin(value)

        then:
        result == expectedResult

        where:
        value          | expectedResult
        ''             | ''
        'W100аА'       | 'W100AA'
        'АВЕКМНОРСТУХ' | 'ABEKMHOPCTYX'
        'авекмнорстух' | 'ABEKMHOPCTYX'
    }

}