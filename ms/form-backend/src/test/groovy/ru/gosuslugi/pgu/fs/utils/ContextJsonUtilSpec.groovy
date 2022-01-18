package ru.gosuslugi.pgu.fs.utils

import com.fasterxml.jackson.databind.ObjectMapper
import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import spock.lang.Specification

class ContextJsonUtilSpec extends Specification {

    ObjectMapper objectMapper = JsonProcessingUtil.getObjectMapper()

    def "test"() {
        given:
        ApplicantAnswer answerExpected = new ApplicantAnswer(value: JsonFileUtil.getJsonFromFile(this.getClass(),expectedAnswerValueFilenameSuffix))
        ApplicantAnswer answer = new ApplicantAnswer(value: JsonFileUtil.getJsonFromFile(this.getClass(),beforeAnswerValueFilenameSuffix))
        ContextJsonUtil<FullAddress> expectedJsonUtil = new ContextJsonUtil(answerExpected, jsonPath, FullAddress.class)
        ContextJsonUtil<FullAddress> contextJsonUtil = new ContextJsonUtil(answer, jsonPath, FullAddress.class)

        when:
        expectedJsonUtil.save(expectedJsonUtil.read())
        FullAddress fullAddress = FullAddressEnrichUtil.enrich(contextJsonUtil.read());
        contextJsonUtil.save(fullAddress);

        then:
        objectMapper.readTree(answerExpected.getValue()) == objectMapper.readTree(answer.getValue())

        where:
        jsonPath        | beforeAnswerValueFilenameSuffix | expectedAnswerValueFilenameSuffix
        "\$['regAddr']" | "-1-answer_value.json"          | "-1-answer_value_expected.json"
        "\$['regAddr']" | "-2-answer_value.json"          | "-2-answer_value_expected.json"
        "\$"            | "-3-answer_value.json"          | "-3-answer_value_expected.json"
    }
}
