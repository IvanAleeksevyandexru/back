package ru.gosuslugi.pgu.kafkacli;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.JacksonUtils;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.gosuslugi.pgu.dto.SpAdapterDto;
import ru.gosuslugi.pgu.dto.SpRequestErrorDto;

@SpringBootApplication
@ConfigurationPropertiesScan
public class KafkaCli {

    public static void main(String[] args) {
        SpringApplication.run(KafkaCli.class, args);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JacksonUtils.enhancedObjectMapper();
    }

    @Bean
    public KafkaConsumer<Long, SpRequestErrorDto> kafkaConsumer(KafkaProperties kafkaProperties) {
        val props = kafkaProperties.buildConsumerProperties();
        return new KafkaConsumer<>(props, new LongDeserializer(), new JsonDeserializer<>(SpRequestErrorDto.class, objectMapper()));
    }

    @Bean
    public KafkaProducer<Long, SpAdapterDto> kafkaProducer(KafkaProperties kafkaProperties) {
        val props = kafkaProperties.buildProducerProperties();
        return new KafkaProducer<>(props, new LongSerializer(), new JsonSerializer<>(objectMapper()));
    }

}
