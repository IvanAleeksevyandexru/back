package ru.gosuslugi.pgu.fs.config.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import ru.gosuslugi.pgu.common.kafka.config.KafkaConsumerCreator;
import ru.gosuslugi.pgu.common.kafka.properties.KafkaConsumerProperties;
import ru.gosuslugi.pgu.dto.SpRequestErrorDto;
import ru.gosuslugi.pgu.dto.SpResponseOkDto;
import ru.gosuslugi.pgu.fs.sp.impl.KafkaSpAdapterErrorListener;
import ru.gosuslugi.pgu.fs.sp.impl.KafkaSpAdapterResponseListener;


@Configuration
@RequiredArgsConstructor
@ConditionalOnExpression("${spring.kafka.producer.sp-adapter-batch.enabled}")
public class KafkaSpAdapterConsumerConfig {

    private final KafkaConsumerCreator kafkaConsumerCreator;

    @Bean
    @ConfigurationProperties(prefix = "spring.kafka.consumer.sp-adapter-response")
    public KafkaConsumerProperties spAdapterResponseConsumerProperties() {
        return new KafkaConsumerProperties();
    }

    @Bean
    public ConsumerFactory<Long, SpResponseOkDto> spAdapterResponseConsumerFactory() {
        return kafkaConsumerCreator.createConsumerFactory(
                new LongDeserializer(),
                new JsonDeserializer<>(SpResponseOkDto.class),
                spAdapterResponseConsumerProperties()
        );
    }

    @Bean
    public ConcurrentMessageListenerContainer<Long, SpResponseOkDto> spAdapterResponseListenerContainer(
            KafkaSpAdapterResponseListener kafkaSpAdapterResponseListener
    ) {
        return kafkaConsumerCreator.createListenerContainer(
                spAdapterResponseConsumerFactory(),
                spAdapterResponseConsumerProperties(),
                kafkaSpAdapterResponseListener
        );
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.kafka.consumer.sp-adapter-error")
    public KafkaConsumerProperties spAdapterErrorConsumerProperties() {
        return new KafkaConsumerProperties();
    }

    @Bean
    public ConsumerFactory<Long, SpRequestErrorDto> spAdapterErrorConsumerFactory() {
        return kafkaConsumerCreator.createConsumerFactory(
                new LongDeserializer(),
                new JsonDeserializer<>(SpRequestErrorDto.class),
                spAdapterResponseConsumerProperties()
        );
    }

    @Bean
    public ConcurrentMessageListenerContainer<Long, SpRequestErrorDto> spAdapterErrorListenerContainer(
            KafkaSpAdapterErrorListener kafkaSpAdapterErrorListener
    ) {
        return kafkaConsumerCreator.createListenerContainer(
                spAdapterErrorConsumerFactory(),
                spAdapterErrorConsumerProperties(),
                kafkaSpAdapterErrorListener
        );
    }

}
