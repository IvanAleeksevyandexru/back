package ru.gosuslugi.pgu.fs.component.time

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest
class DateTimePeriodTest extends Specification {

    @Autowired
    private DateTimePeriod dateTimePeriod;
    @Shared
    private String screenId = "scr0"

    def "validation - positive"() {
        given:
        String jsonString = "{\"startDateTime\":\"2021-03-10T07:20\",\"endDateTime\":\"2021-03-10T07:30\"}"
        HashMap incorrectAnswer = new HashMap<String, String>()

        when:
        dateTimePeriod.validateAfterSubmit(incorrectAnswer, screenId, jsonString)

        then:
        incorrectAnswer.size() == 0
    }

    def "validate - value is null"() {
        given:
        String jsonString = null
        HashMap incorrectAnswer = new HashMap<String, String>()

        when:
        dateTimePeriod.validateAfterSubmit(incorrectAnswer, screenId, jsonString)

        then:
        incorrectAnswer.size() == 1
    }
    def "validate - value is empty"() {
        given:
        String jsonString = ""
        HashMap incorrectAnswer = new HashMap<String, String>()

        when:
        dateTimePeriod.validateAfterSubmit(incorrectAnswer, screenId, jsonString)

        then:
        incorrectAnswer.size() == 1
    }
    def "validate - start is null"() {
        given:
        String jsonString = "{\"endDateTime\":\"2022-03-09T21:00\"}"
        HashMap incorrectAnswer = new HashMap<String, String>()

        when:
        dateTimePeriod.validateAfterSubmit(incorrectAnswer, screenId, jsonString)

        then:
        incorrectAnswer.size() == 1
    }
    def "validate - end is null"() {
        given:
        String jsonString = "{\"startDateTime\":\"2021-03-10T07:30\"}"
        HashMap incorrectAnswer = new HashMap<String, String>()

        when:
        dateTimePeriod.validateAfterSubmit(incorrectAnswer, screenId, jsonString)

        then:
        incorrectAnswer.size() == 1
    }
    def "validate - start is not textual"() {
        given:
        String jsonString = "{\"startDateTime\": 1,\"endDateTime\":\"2022-03-09T21:00\"}"
        HashMap incorrectAnswer = new HashMap<String, String>()

        when:
        dateTimePeriod.validateAfterSubmit(incorrectAnswer, screenId, jsonString)

        then:
        incorrectAnswer.size() == 1
    }
    def "validate - end is not textual"() {
        given:
        String jsonString = "{\"startDateTime\":\"2021-03-10T07:30\",\"endDateTime\": 2}"
        HashMap incorrectAnswer = new HashMap<String, String>()

        when:
        dateTimePeriod.validateAfterSubmit(incorrectAnswer, screenId, jsonString)

        then:
        incorrectAnswer.size() == 1
    }
    def "validate - start after end"() {
        given:
        String jsonString = "{\"startDateTime\":\"2021-03-10T07:30\",\"endDateTime\":\"2021-03-10T07:20\"}"
        HashMap incorrectAnswer = new HashMap<String, String>()

        when:
        dateTimePeriod.validateAfterSubmit(incorrectAnswer, screenId, jsonString)

        then:
        incorrectAnswer.size() == 1
    }
}
