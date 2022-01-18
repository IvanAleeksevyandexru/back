package ru.gosuslugi.pgu.fs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.fs.service.LkNotifierClient;
import ru.gosuslugi.pgu.fs.service.impl.LkNotifierClientImpl;
import ru.gosuslugi.pgu.fs.service.impl.LkNotifierClientStub;

@Configuration
public class LkNotifierServiceConfig {

    @Bean
    @ConditionalOnProperty(value = "lk-notifier.integration.enabled", havingValue = "true", matchIfMissing = true)
    public LkNotifierClient analyticClusterClient(RestTemplate restTemplate, @Value("${lk-notifier.integration.url}") String lkNotifierUrl) {
        return new LkNotifierClientImpl(restTemplate, lkNotifierUrl);
    }

    @Bean
    @ConditionalOnProperty(value = "lk-notifier.integration.enabled", havingValue = "false")
    public LkNotifierClient analyticClusterClientStub() {
        return new LkNotifierClientStub();
    }
}
