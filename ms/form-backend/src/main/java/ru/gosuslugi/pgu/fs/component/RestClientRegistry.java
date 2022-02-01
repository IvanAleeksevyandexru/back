package ru.gosuslugi.pgu.fs.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.core.exception.handler.RestResponseErrorHandler;
import ru.gosuslugi.pgu.common.core.interceptor.creator.RestTemplateCreator;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class RestClientRegistry {

    private static final Integer DEFAULT_TIMEOUT = -1;
    private static final ConcurrentMap<Integer, RestTemplate> registry = new ConcurrentHashMap<>();

    private final RestTemplateBuilder restTemplateBuilder;
    private final List<ClientHttpRequestInterceptor> interceptors;

    @Autowired
    public RestClientRegistry(RestTemplate restTemplate, RestTemplateBuilder restTemplateBuilder) {
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
        restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());
        registry.put(DEFAULT_TIMEOUT, restTemplate);
        interceptors = restTemplate.getInterceptors();
        this.restTemplateBuilder = restTemplateBuilder;
    }

    public RestTemplate getRestTemplate() {
        return register(DEFAULT_TIMEOUT);
    }

    public RestTemplate getRestTemplate(Integer timeout) {
        return register(timeout);
    }

    private RestTemplate register(Integer timeout) {
        var instance = registry.get(timeout);
        if (instance == null) {
            synchronized (this) {
                instance = registry.get(timeout);
                if (instance == null) {
                    instance = registry.computeIfAbsent(timeout, v -> createFor(timeout));
                }
            }
        }
        return instance;
    }

    private RestTemplate createFor(Integer timeout) {
        var connectTimeout = Duration.of(timeout, ChronoUnit.MILLIS);

        var restTemplate = restTemplateBuilder
                .requestFactory(() -> RestTemplateCreator.createConnectionFactory(timeout, timeout, DEFAULT_TIMEOUT))
                .setConnectTimeout(connectTimeout)
                .setReadTimeout(connectTimeout)
                .build();
        restTemplate.setInterceptors(interceptors);
        restTemplate.setErrorHandler(new RestResponseErrorHandler());

        return restTemplate;
    }
}
