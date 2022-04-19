package ru.gosuslugi.pgu.fs.common.utils

import ru.gosuslugi.pgu.common.core.exception.JsonParsingException
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil
import spock.lang.Specification

import static java.util.Collections.emptyMap

class AnswerUtilSpec extends Specification {

    def 'Can convert json to list'() {
        given:
        def result

        when:
        result = AnswerUtil.toList(
                new AbstractMap.SimpleEntry<String, ApplicantAnswer>('key', new ApplicantAnswer(value: json)), elseEmptyList)

        then:
        result == expectedResult

        where:
        json                   | elseEmptyList | expectedResult
        ''                     | true          | []
        ''                     | false         | null
        null                   | true          | []
        null                   | false         | null
        '[]'                   | true          | []
        '["a", "b"]'           | true          | ['a', 'b']
        '[{"a": 1}, {"b": 2}]' | true          | [[a: 1], [b: 2]]
    }

    def 'AnswerUtil.toList/2: If value is incorrect - thrown exception '() {
        when:
        AnswerUtil.toList(new AbstractMap.SimpleEntry<String, ApplicantAnswer>('key', new ApplicantAnswer(value: 'x')), true)

        then:
        thrown(JsonParsingException)
    }

    def "Test create answer"() {
        when:
        def entry = AnswerUtil.createAnswerEntry("key", "value")

        then:
        entry.getKey() == "key"
        entry.getValue().getValue() == "value"
        entry.getValue().getVisited()
    }

    def "Test to map"() {
        expect:
        AnswerUtil.toMap(null,false) == null
        AnswerUtil.toMap(null,true) == emptyMap()

        AnswerUtil.toMap(new AbstractMap.SimpleEntry<String, ApplicantAnswer>("key", null), false) == null
        AnswerUtil.toMap(AnswerUtil.createAnswerEntry("key", null), false) == null
        AnswerUtil.toMap(AnswerUtil.createAnswerEntry("key", "{}"), false) == emptyMap()

        AnswerUtil.toMap(AnswerUtil.createAnswerEntry("key", "{\"value\": 1}"), false) == ["value": 1]
    }
}