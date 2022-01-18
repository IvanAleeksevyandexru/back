package ru.gosuslugi.pgu.fs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.fs.config.properties.RateLimitProperties;
import ru.gosuslugi.pgu.fs.service.ratelimit.RateLimitService;
import ru.gosuslugi.pgu.fs.service.ratelimit.impl.RateLimitServiceImpl;
import ru.gosuslugi.pgu.fs.service.ratelimit.impl.RateLimitServiceStub;

@Configuration
public class RateLimitServiceConfig {

    @Bean
    @ConditionalOnProperty(value = "mock.ratelimit.enabled", havingValue = "false", matchIfMissing = true)
    public RateLimitService rateLimitService(RestTemplate restTemplate, RateLimitProperties rateLimitProperties) {
        return new RateLimitServiceImpl(restTemplate, rateLimitProperties);
    }

    @Bean
    @ConditionalOnProperty(value = "mock.ratelimit.enabled", havingValue = "true")
    public RateLimitService rateLimitServiceStub() {
        return new RateLimitServiceStub();
    }
}
