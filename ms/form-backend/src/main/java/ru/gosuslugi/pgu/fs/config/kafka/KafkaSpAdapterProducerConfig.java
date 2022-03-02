package ru.gosuslugi.pgu.fs.config.kafka;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.gosuslugi.pgu.common.kafka.config.KafkaProducerCreator;
import ru.gosuslugi.pgu.common.kafka.properties.KafkaProducerProperties;
import ru.gosuslugi.pgu.dto.SpAdapterDto;

import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@ConditionalOnExpression("${spring.kafka.producer.sp-adapter-batch.enabled}")
public class KafkaSpAdapterProducerConfig {

    private final KafkaProducerCreator kafkaProducerCreator;

    @Bean
    @ConfigurationProperties(prefix = "spring.kafka.producer.sp-adapter-batch")
    public KafkaProducerProperties spAdapterBatchProducerProperties() {
        return new KafkaProducerProperties();
    }

    @Bean
    @ConditionalOnProperty("spring.kafka.producer.sp-adapter-batch.auto-create-topics")
    public NewTopic serviceProcessingBatchTopic() {
        return spAdapterBatchProducerProperties().toNewTopic();
    }

    @Bean
    @ConditionalOnProperty("spring.kafka.producer.sp-adapter-batch.enabled")
    public ProducerFactory<Long, List<SpAdapterDto>> spBatchProducerFactory() {
        return kafkaProducerCreator.createProducerFactory(
                new LongSerializer(),
                new JsonSerializer<>(),
                Map.of(JsonSerializer.ADD_TYPE_INFO_HEADERS, Boolean.FALSE)
        );

    }

    @Bean
    @ConditionalOnProperty("spring.kafka.producer.sp-adapter-batch.enabled")
    public KafkaTemplate<Long, List<SpAdapterDto>> spBatchKafkaTemplate() {
        val t = kafkaProducerCreator.createKafkaTemplate(spBatchProducerFactory());
        t.setDefaultTopic(spAdapterBatchProducerProperties().getTopic());
        return t;
    }
}
