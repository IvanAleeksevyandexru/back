package ru.gosuslugi.pgu.fs.service.ratelimit.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ratelimit.RateLimitOverHeadDto;
import ru.gosuslugi.pgu.fs.service.ratelimit.RateLimitAnalyticProducer;
@Component
@AllArgsConstructor
@ConditionalOnProperty(prefix = "spring.kafka.producer.rate-limit-analytic", name = "enabled", havingValue = "false")
@Slf4j
public class RateLimitAnalyticProducerStub implements RateLimitAnalyticProducer {
    @Override
    public void send(RateLimitOverHeadDto rateLimitOverHeadDto) {

    }
}
