package ru.gosuslugi.pgu.fs.common.variable

import ru.gosuslugi.pgu.dto.ScenarioDto
import spock.lang.Specification

import java.time.OffsetDateTime
import java.util.regex.Pattern

class TodayTimeStampVariableSpec extends Specification {
    ScenarioDto scenarioDto  = Stub()
    TodayTimeStampVariable todayTimeStampVariable = new TodayTimeStampVariable()

    def "getType should return correct variable type"() {
        when:
        VariableType type = todayTimeStampVariable.getType()
        then:
        type == VariableType.todayTimeStamp;
    }

    def "getValue should return string time who is less than or equal to current time"() {
        when:
        String utcTimeStr = todayTimeStampVariable.getValue(scenarioDto)
        OffsetDateTime utcTime = OffsetDateTime.parse(utcTimeStr, TodayTimeStampVariable.DATE_TIME_FORMATTER)
        OffsetDateTime currentUtcTime = OffsetDateTime.now(TodayTimeStampVariable.ZONE_OFFSET)
        then:
        utcTime.compareTo(currentUtcTime) <= 0
        utcTime.getOffset() == currentUtcTime.getOffset()
    }
}
