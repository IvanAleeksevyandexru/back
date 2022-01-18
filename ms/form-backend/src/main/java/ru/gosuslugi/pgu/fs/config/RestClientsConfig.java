package ru.gosuslugi.pgu.fs.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.gosuslugi.pgu.common.core.exception.handler.RestResponseErrorHandler;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.core.interceptor.creator.RestTemplateCreator;

@Configuration
public class RestClientsConfig {

    @Bean
    public RestTemplate restTemplate(ConfigurableEnvironment env, RestTemplateCustomizer... customizers) {
        RestTemplate restTemplate = RestTemplateCreator.create("rest-client", objectMapper(), env, customizers);

        restTemplate.setErrorHandler(new RestResponseErrorHandler());
        return restTemplate;
    }

    @Bean
    public RestTemplate esiaClientRestTemplate(ConfigurableEnvironment env, RestTemplateCustomizer... customizers) {
        // NONE for URL encoding, you must encode parameters by yourself
        DefaultUriBuilderFactory builderFactory = new DefaultUriBuilderFactory();
        builderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        RestTemplate esiaRestTemplate = RestTemplateCreator.create("esia-rest-client", objectMapper(), env, builderFactory, customizers);
        esiaRestTemplate.setErrorHandler(new RestResponseErrorHandler());
        return esiaRestTemplate;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonProcessingUtil.getObjectMapper();
    }

}
