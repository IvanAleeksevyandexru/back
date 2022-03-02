package ru.gosuslugi.pgu.fs.suggests.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.gosuslugi.pgu.common.kafka.config.KafkaProducerCreator;
import ru.gosuslugi.pgu.common.kafka.properties.KafkaProducerProperties;
import ru.gosuslugi.pgu.dto.suggest.SuggestDraftDto;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.kafka.producer.suggests", name = "enabled", havingValue = "true")
public class KafkaSuggestsProducerConfig {

    private final KafkaProducerCreator kafkaProducerCreator;

    @Bean
    @ConfigurationProperties(prefix = "spring.kafka.producer.suggests")
    public KafkaProducerProperties suggestsProducerProperties() {
        return new KafkaProducerProperties();
    }


    @Bean
    public ProducerFactory<Long, SuggestDraftDto> suggestsProducerFactory() {
        return kafkaProducerCreator.createProducerFactory(new LongSerializer(), new JsonSerializer<>());
    }

    @Bean
    public KafkaTemplate<Long, SuggestDraftDto> suggestsKafkaTemplate() {
        return kafkaProducerCreator.createKafkaTemplate(suggestsProducerFactory());
    }
}
