package ru.gosuslugi.pgu.fs.config.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.gosuslugi.pgu.common.kafka.config.KafkaProducerCreator;
import ru.gosuslugi.pgu.common.kafka.properties.KafkaProducerProperties;
import ru.gosuslugi.pgu.dto.ratelimit.RateLimitOverHeadDto;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.kafka.producer.rate-limit-analytic", name = "enabled", havingValue = "true")
public class KafkaRateLimitAnalyticProducerConfig {

    private final KafkaProducerCreator kafkaProducerCreator;

    @Bean
    @ConfigurationProperties(prefix = "spring.kafka.producer.rate-limit-analytic")
    public KafkaProducerProperties rateLimitProducerProperties() {
        return new KafkaProducerProperties();
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.kafka.producer.rate-limit-analytic", name = "auto-create-topics", havingValue = "true")
    public NewTopic rateLimitAnalyticsTopic() {
        return rateLimitProducerProperties().toNewTopic();
    }


    @Bean
    public ProducerFactory<String, RateLimitOverHeadDto> rateLimitOverHeadDtoProducerFactory() {
        return kafkaProducerCreator.createProducerFactory(new StringSerializer(), new JsonSerializer<>());
    }


    @Bean
    public KafkaTemplate<String, RateLimitOverHeadDto> rateLimitOverHeadDtoKafkaTemplate() {
        return kafkaProducerCreator.createKafkaTemplate(rateLimitOverHeadDtoProducerFactory());
    }

}
