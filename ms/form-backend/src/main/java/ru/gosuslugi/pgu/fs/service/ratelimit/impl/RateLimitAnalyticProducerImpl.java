package ru.gosuslugi.pgu.fs.service.ratelimit.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.kafka.properties.KafkaProducerProperties;
import ru.gosuslugi.pgu.dto.ratelimit.RateLimitOverHeadDto;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.fs.service.ratelimit.RateLimitAnalyticProducer;

import java.util.concurrent.ExecutionException;

@Component
@AllArgsConstructor
@ConditionalOnProperty(prefix = "spring.kafka.producer.rate-limit-analytic", name = "enabled", havingValue = "true")
@Slf4j
public class RateLimitAnalyticProducerImpl implements RateLimitAnalyticProducer {

    private final KafkaTemplate<String, RateLimitOverHeadDto> rateLimitOverHeadDtoKafkaTemplate;
    private final KafkaProducerProperties rateLimitProducerProperties;

    @Override
    public void send(RateLimitOverHeadDto rateLimitOverHeadDto) {
        try {
            rateLimitOverHeadDtoKafkaTemplate.send(
                    rateLimitProducerProperties.getTopic(),
                    rateLimitOverHeadDto.getUserId(),
                    rateLimitOverHeadDto
            ).get();
            log.info("Отправлено в Rate Limit Analytic: {}", rateLimitOverHeadDto);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Ошибка отправки в Rate Limit Analytic: {}", rateLimitOverHeadDto, e);
            throw new FormBaseException("Ошибка отправки в Rate Limit Analytic", e);
        }
    }
}
