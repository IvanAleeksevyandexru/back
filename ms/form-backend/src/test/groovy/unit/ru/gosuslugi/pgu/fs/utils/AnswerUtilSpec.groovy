package unit.ru.gosuslugi.pgu.fs.utils

import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil
import spock.lang.Specification

class AnswerUtilSpec extends Specification{

    def "createAnswerEntry"() {
        when:
        Map.Entry<String, ApplicantAnswer> entry = AnswerUtil.createAnswerEntry("key", "value")

        then:
        entry.getKey() == "key"
        entry.getValue().value == "value"
        entry.getValue().visited
    }

    def "testsToString"() {
        given:
        def value

        when:
        value = AnswerUtil.getValueOrNull(entry)

        then:
        value == expectedResult

        where:
        entry                                                              | expectedResult
        null                                                               | null
        new AbstractMap.SimpleEntry<String, ApplicantAnswer>("key", null)  | null
        AnswerUtil.createAnswerEntry("key", null)                          | null
        AnswerUtil.createAnswerEntry("key", "value")                       | "value"
    }

    def "testsGetValue"() {
        given:
        def value

        when:
        value = AnswerUtil.getValue(entry)

        then:
        value == expectedResult

        where:
        entry                                                              | expectedResult
        null                                                               | ""
        new AbstractMap.SimpleEntry<String, ApplicantAnswer>("key", null)  | ""
        AnswerUtil.createAnswerEntry("key", null)                          | ""
        AnswerUtil.createAnswerEntry("key", "value")                       | "value"
    }

    def "testsToMap"() {
        given:
        def value

        when:
        value = AnswerUtil.toMap(entry, elseEmptyMap)

        then:
        value == expectedResult

        where:
        entry                                                               | elseEmptyMap  | expectedResult
        null                                                                | false         | null
        null                                                                | true          | new HashMap<>()
        new AbstractMap.SimpleEntry<String, ApplicantAnswer>("key", null)   | false         | null
        AnswerUtil.createAnswerEntry("key", null)                           | false         | null
        AnswerUtil.createAnswerEntry("key", "{}")                           | false         | new HashMap<>()
        AnswerUtil.createAnswerEntry("key", "{\"value\": 1}")               | false         | ["value": 1]
    }

    def "testsToStringMap"() {
        given:
        def value

        when:
        value = AnswerUtil.toStringMap(entry, elseEmptyMap)

        then:
        value == expectedResult

        where:
        entry                                                               | elseEmptyMap  | expectedResult
        null                                                                | false         | null
        null                                                                | true          | new HashMap<>()
        new AbstractMap.SimpleEntry<String, ApplicantAnswer>("key", null)   | false         | null
        AnswerUtil.createAnswerEntry("key", null)                           | false         | null
        AnswerUtil.createAnswerEntry("key", "{}")                           | false         | new HashMap<>()
        AnswerUtil.createAnswerEntry("key", "{\"value\": 1}")               | false         | ["value": "1"]
    }

    def "testsToList"() {
        given:
        def value

        when:
        value = AnswerUtil.toList(entry, elseEmptyMap)

        then:
        value == expectedResult

        where:
        entry                                                               | elseEmptyMap  | expectedResult
        null                                                                | false         | null
        null                                                                | true          | new ArrayList<>()
        new AbstractMap.SimpleEntry<String, ApplicantAnswer>("key", null)   | false         | null
        AnswerUtil.createAnswerEntry("key", null)                           | false         | null
        AnswerUtil.createAnswerEntry("key", "[]")                           | false         | new ArrayList<>()
        AnswerUtil.createAnswerEntry("key", "[\"value\", 1]")               | false         | ["value", 1]
    }

    def "testsToMapList"() {
        given:
        def value

        when:
        value = AnswerUtil.toMapList(entry, elseEmptyMap)

        then:
        value == expectedResult

        where:
        entry                                                               | elseEmptyMap  | expectedResult
        null                                                                | false         | null
        null                                                                | true          | new ArrayList<>()
        new AbstractMap.SimpleEntry<String, ApplicantAnswer>("key", null)   | false         | null
        AnswerUtil.createAnswerEntry("key", null)                           | false         | null
        AnswerUtil.createAnswerEntry("key", "[]")                           | false         | new ArrayList<>()
        AnswerUtil.createAnswerEntry("key", "[{\"value\": 1}]")             | false         | [["value": 1]]
    }

    def "testsToStringMapList"() {
        given:
        def value

        when:
        value = AnswerUtil.toStringMapList(entry, elseEmptyMap)

        then:
        value == expectedResult

        where:
        entry                                                               | elseEmptyMap  | expectedResult
        null                                                                | false         | null
        null                                                                | true          | new ArrayList<>()
        new AbstractMap.SimpleEntry<String, ApplicantAnswer>("key", null)   | false         | null
        AnswerUtil.createAnswerEntry("key", null)                           | false         | null
        AnswerUtil.createAnswerEntry("key", "[]")                           | false         | new ArrayList<>()
        AnswerUtil.createAnswerEntry("key", "[{\"value\": 1}]")             | false         | [["value": "1"]]
    }
}
