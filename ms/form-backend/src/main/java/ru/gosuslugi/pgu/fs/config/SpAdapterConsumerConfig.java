package ru.gosuslugi.pgu.fs.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import ru.gosuslugi.pgu.common.kafka.config.AbstractConsumerConfig;
import ru.gosuslugi.pgu.dto.SpRequestErrorDto;
import ru.gosuslugi.pgu.dto.SpResponseOkDto;
import ru.gosuslugi.pgu.fs.config.properties.SpAdapterConsumerProps;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = "spring.kafka.consumer.sp-adapter.enabled", havingValue = "true")
public class SpAdapterConsumerConfig extends AbstractConsumerConfig<Long, SpResponseOkDto> {

    @Value(value = "${spring.kafka.brokers}")
    private String brokers;

    private final SpAdapterConsumerProps consumerProps;

    @Bean
    public ConsumerFactory<Long, SpResponseOkDto> spResponseOkDtoConsumerFactory(){
        return this.getDefaultKafkaFactory(consumerProps, SpResponseOkDto.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, SpResponseOkDto> spResponseListenerContainerFactory() {
        return this.getKafkaListener(spResponseOkDtoConsumerFactory());
    }

    @Bean
    public ConsumerFactory<Long, SpRequestErrorDto> spRequestErrorConsumerFactory() {
        return this.getDefaultKafkaFactory(consumerProps, SpRequestErrorDto.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, SpRequestErrorDto> spRequestErrorKafkaListenerContainerFactory() {
        return this.getAdditionalKafkaListener(spRequestErrorConsumerFactory());
    }

}
