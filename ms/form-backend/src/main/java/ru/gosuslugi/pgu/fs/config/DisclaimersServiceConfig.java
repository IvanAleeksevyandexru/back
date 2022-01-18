package ru.gosuslugi.pgu.fs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.fs.service.DisclaimersClient;
import ru.gosuslugi.pgu.fs.service.impl.DisclaimersClientImpl;
import ru.gosuslugi.pgu.fs.service.impl.DisclaimersClientStub;

@Configuration
public class DisclaimersServiceConfig {

    @Bean
    @ConditionalOnProperty(value = "disclaimers.integration.enabled", havingValue = "true", matchIfMissing = true)
    public DisclaimersClient disclaimersClient(RestTemplate restTemplate, UserPersonalData userPersonalData, @Value("${disclaimers.integration.url}") String disclaimersUrl) {
        return new DisclaimersClientImpl(restTemplate, userPersonalData, disclaimersUrl);
    }

    @Bean
    @ConditionalOnProperty(value = "disclaimers.integration.enabled", havingValue = "false")
    public DisclaimersClient disclaimersClientStub() {
        return new DisclaimersClientStub();
    }
}
