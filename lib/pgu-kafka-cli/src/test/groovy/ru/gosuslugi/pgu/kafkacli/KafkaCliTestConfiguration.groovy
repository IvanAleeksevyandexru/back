package ru.gosuslugi.pgu.kafkacli

import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.TestPropertySource

@TestConfiguration
@TestPropertySource(properties = ["spring.main"])
class KafkaCliTestConfiguration {

    @Bean
    @Primary
    KafkaCliRunner kafkaCliRunner() {
        return Mockito.mock(KafkaCliRunner.class)
    }

}
