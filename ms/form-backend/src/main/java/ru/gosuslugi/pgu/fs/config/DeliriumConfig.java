package ru.gosuslugi.pgu.fs.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.fs.delirium.DeliriumClient;
import ru.gosuslugi.pgu.fs.delirium.client.DeliriumClientImpl;
import ru.gosuslugi.pgu.fs.delirium.client.DeliriumClientStub;
import ru.gosuslugi.pgu.fs.delirium.configuration.DeliriumClientConfiguration;

@Configuration
public class DeliriumConfig {

    @Bean
    @ConditionalOnProperty(value = "delirium.enabled", havingValue = "true", matchIfMissing = true)
    public DeliriumClient deliriumClient(RestTemplate restTemplate, DeliriumClientConfiguration properties) {
        return new DeliriumClientImpl(restTemplate, properties);
    }

    @Bean
    @ConditionalOnProperty(value = "delirium.enabled", havingValue = "false")
    public DeliriumClient deliriumClientStub() {
        return new DeliriumClientStub();
    }

}
