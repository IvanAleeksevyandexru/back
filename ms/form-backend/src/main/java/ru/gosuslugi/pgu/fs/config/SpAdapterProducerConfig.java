package ru.gosuslugi.pgu.fs.config;


import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.gosuslugi.pgu.common.kafka.properties.AbstractProducerProps;
import ru.gosuslugi.pgu.dto.SpAdapterDto;
import ru.gosuslugi.pgu.fs.config.properties.SpAdapterBatchProducerProps;
import ru.gosuslugi.pgu.fs.config.properties.SpAdapterProducerProps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SpAdapterProducerConfig {

    @Value(value = "${spring.kafka.brokers}")
    private String brokers;
    private final SpAdapterProducerProps producerProps;
    private final SpAdapterBatchProducerProps batchProducerProps;

    @Bean
    @ConditionalOnProperty("spring.kafka.producer.sp-adapter.auto-create-topics")
    public NewTopic serviceProcessingTopic() {
        return createTopic(producerProps);
    }

    @Bean
    @ConditionalOnProperty("spring.kafka.producer.sp-adapter-batch.auto-create-topics")
    public NewTopic serviceProcessingBatchTopic() {
        return createTopic(batchProducerProps);
    }

    @Bean
    @ConditionalOnProperty("spring.kafka.producer.sp-adapter.enabled")
    public ProducerFactory<Long, SpAdapterDto> producerFactory() {
        return createProducerFactory(new LongSerializer(), new JsonSerializer<>());
    }

    @Bean
    @ConditionalOnProperty("spring.kafka.producer.sp-adapter.enabled")
    public KafkaTemplate<Long, SpAdapterDto> kafkaTemplate() {
        return createTemplate(producerFactory(), producerProps.getTopicName());
    }

    @Bean
    @ConditionalOnProperty("spring.kafka.producer.sp-adapter-batch.enabled")
    public ProducerFactory<Long, List<SpAdapterDto>> batchProducerFactory() {
        return createProducerFactory(new LongSerializer(), new JsonSerializer<>());
    }

    @Bean
    @ConditionalOnProperty("spring.kafka.producer.sp-adapter-batch.enabled")
    public KafkaTemplate<Long, List<SpAdapterDto>> batchKafkaTemplate() {
        return createTemplate(batchProducerFactory(), batchProducerProps.getTopicName());
    }

    private NewTopic createTopic(AbstractProducerProps props) {
        return new NewTopic(props.getTopicName(), props.getTopicPartitions(), props.getTopicReplicationFactor());
    }

    private <K, V> ProducerFactory<K, V> createProducerFactory(Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, Boolean.FALSE);
        return new DefaultKafkaProducerFactory<>(configProps, keySerializer, valueSerializer);
    }

    private <K, V> KafkaTemplate<K, V> createTemplate(ProducerFactory<K, V> producerFactory, String defaultTopic) {
        val t = new KafkaTemplate<>(producerFactory);
        t.setDefaultTopic(defaultTopic);
        return t;
    }

}

