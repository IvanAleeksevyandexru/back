package ru.gosuslugi.pgu.common.certificate.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.certificate.dto.BarBarBokRequestDto;
import ru.gosuslugi.pgu.common.certificate.dto.BarBarBokResponseDto;
import ru.gosuslugi.pgu.common.certificate.service.marshaller.KinderGartenXmlMarshaller;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.common.core.interceptor.creator.RestTemplateCreator;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.info;

@Component
@Primary
@Slf4j
public class BarbarbokClientImpl implements BarbarbokClient {
    private static final String API_URL = "barbarbok/internal/api/barbarbok/v1/get";

    private final RestTemplateBuilder restTemplateBuilder;
    private final RestTemplate defaultRestTemplate;

    @Value("${pgu.barbarbok-url}")
    @Getter
    @Setter
    private String host;

    public BarbarbokClientImpl(RestTemplateBuilder restTemplateBuilder,
                               RestTemplate restTemplate,
                               ConfigurableEnvironment env,
                               RestTemplateCustomizer... customizers) {
        this.defaultRestTemplate = restTemplate;
        this.restTemplateBuilder = RestTemplateCreator.create(restTemplateBuilder, env, customizers);
    }

    @Override
    public BarBarBokResponseDto get(BarBarBokRequestDto dto) {
        return get(dto, -1);
    }

    @Override
    public BarBarBokResponseDto get(BarBarBokRequestDto dto, int timeout) {
        AtomicReference<ResponseEntity<BarBarBokResponseDto>> barBarBokResponse = new AtomicReference<>();
        RestTemplate restTemplate = RestTemplateCreator.createOrDefault(restTemplateBuilder, timeout, defaultRestTemplate);
        try {
            exchange(dto, barBarBokResponse, restTemplate);
        } catch (ExternalServiceException e) {
            log.error("Ошибка при запросе сертификата ЕАИСДО: {},\n {}", dto, e.getMessage());

            BarBarBokResponseDto barBarBokResponseDto =  BarBarBokResponseDto.builder()
                    .status(e.getStatus().name())
                    .errorMessage(e.getMessage())
                    .build();

            ResponseEntity<BarBarBokResponseDto> entity = ResponseEntity.of(Optional.of(barBarBokResponseDto));
            barBarBokResponse.set(entity);
        }
        return barBarBokResponse.get().getBody();
    }

    @Override
    public BarBarBokResponseDto getUnsafe(BarBarBokRequestDto dto, int timeout) {
        AtomicReference<ResponseEntity<BarBarBokResponseDto>> barBarBokResponse = new AtomicReference<>();
        RestTemplate restTemplate = RestTemplateCreator.createOrDefault(restTemplateBuilder, timeout, defaultRestTemplate);
        exchange(dto, barBarBokResponse, restTemplate);
        return barBarBokResponse.get().getBody();
    }

    private void exchange(BarBarBokRequestDto dto, AtomicReference<ResponseEntity<BarBarBokResponseDto>> barBarBokResponse, RestTemplate restTemplate) {
        ResponseEntity<BarBarBokResponseDto> response = restTemplate.exchange(
                host + API_URL, HttpMethod.POST, new HttpEntity<>(dto), BarBarBokResponseDto.class);
        barBarBokResponse.set(response);
        if (barBarBokResponse.get().getStatusCode().is2xxSuccessful()) {
            String data = Objects.requireNonNull(barBarBokResponse.get().getBody()).getData();
            info(log, () -> String.format("Response received from barbarbok: %s", data));
        }
    }
}
