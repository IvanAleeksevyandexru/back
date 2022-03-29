package ru.gosuslugi.pgu.fs.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.gosuslugi.pgu.dto.ratelimit.RateLimitOverHeadDto;
import ru.gosuslugi.pgu.fs.config.properties.RateLimitAnalyticProducerProps;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.kafka.producer.rate-limit-analytic", name = "enabled", havingValue = "true")
public class RateLimitAnalyticProducerConfig {

    @Value(value = "${spring.kafka.brokers}")
    private String brokers;
    private final RateLimitAnalyticProducerProps producerProps;

    @Bean
    @ConditionalOnProperty(prefix = "spring.kafka.producer.rate-limit-analytic", name = "auto-create-topics", havingValue = "true")
    public NewTopic rateLimitAnalyticsTopic() {
        return new NewTopic(
                producerProps.getTopicName(),
                producerProps.getTopicPartitions(),
                producerProps.getTopicReplicationFactor()
        );
    }

    @Bean
    public ProducerFactory<String, RateLimitOverHeadDto> rateLimitOverHeadDtoProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, RateLimitOverHeadDto> rateLimitOverHeadDtoKafkaTemplate() {
        return new KafkaTemplate<>(rateLimitOverHeadDtoProducerFactory());
    }

}
