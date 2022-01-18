package ru.gosuslugi.pgu.kafkacli

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import java.time.OffsetDateTime

@SpringBootTest(
        classes = [KafkaCli, KafkaCliTestConfiguration],
        properties = [
                "spring.main.allow-bean-definition-overriding=true",
                "app.timestamp.since=2020-01-10T10:00:00+03:00"
        ]
)
class KafkaCliTest extends Specification {

    @Autowired
    KafkaCliProperties props

    def "TestParseTimestamp"() {
        expect:
        props.timestamp.since == OffsetDateTime.parse("2020-01-10T10:00:00+03:00")
    }

}
