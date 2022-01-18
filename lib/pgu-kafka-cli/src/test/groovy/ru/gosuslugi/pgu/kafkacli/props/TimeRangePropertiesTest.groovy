package ru.gosuslugi.pgu.kafkacli.props

import spock.lang.Specification

import java.time.OffsetDateTime

class TimeRangePropertiesTest extends Specification {

    def "includes"() {
        when:
        def p = new TimeRangeProperties(
                since: since != null ? OffsetDateTime.parse(since) : null,
                until: until != null ? OffsetDateTime.parse(until) : null)

        then:
        p.includes(value != null ? OffsetDateTime.parse(value).toInstant() : null) == includes

        where:
        since                       | until                       | value                       | includes
        "2021-03-01T01:00:00+03:00" | "2021-03-01T03:00:00+03:00" | "2021-03-01T01:00:00+03:00" | true
        "2021-03-01T01:00:00+03:00" | "2021-03-01T03:00:00+03:00" | "2021-03-01T03:00:00+03:00" | true
        "2021-03-01T01:00:00+03:00" | "2021-03-01T03:00:00+03:00" | "2021-03-01T02:00:00+03:00" | true
        "2021-03-01T01:00:00+03:00" | "2021-03-01T03:00:00+03:00" | "2021-03-01T04:00:00+03:00" | false
        "2021-03-01T01:00:00+03:00" | null                        | "2021-03-01T04:00:00+03:00" | true
        "2021-03-01T01:00:00+03:00" | null                        | "2021-03-01T00:00:00+03:00" | false
        null                        | "2021-03-01T03:00:00+03:00" | "2021-03-01T00:00:00+03:00" | true
        null                        | "2021-03-01T03:00:00+03:00" | "2021-03-01T04:00:00+03:00" | false
    }

}
