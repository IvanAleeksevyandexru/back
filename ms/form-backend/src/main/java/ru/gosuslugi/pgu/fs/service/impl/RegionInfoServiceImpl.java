package ru.gosuslugi.pgu.fs.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.core.exception.EntityNotFoundException;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.common.core.interceptor.creator.RestTemplateCreator;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.kindergarten.EpguRegionResponse;
import ru.gosuslugi.pgu.fs.service.RegionInfoService;

@Service
@Slf4j
public class RegionInfoServiceImpl implements RegionInfoService {
    private static final String VERSION_CHECK_URL = "api/nsi/v1/epgu/region/{regionCode}/service/{serviceCode}/";

    @Value("${pgu.dictionary-url}")
    private String nsiApiUrl;

    private final RestTemplateBuilder restTemplateBuilder;
    private final RestTemplate defaultRestTemplate;

    public RegionInfoServiceImpl(RestTemplateBuilder restTemplateBuilder,
                                 RestTemplate restTemplate,
                                 ConfigurableEnvironment env,
                                 RestTemplateCustomizer... customizers) {
        this.defaultRestTemplate = restTemplate;
        this.restTemplateBuilder = RestTemplateCreator.create(restTemplateBuilder, env, customizers);
    }

    @Override
    public int getIntegerSmevVersion(String regionCode, String serviceCode) {
        RestTemplate restTemplate = RestTemplateCreator.createOrDefault(restTemplateBuilder, -1, defaultRestTemplate);
        try {
            var response = restTemplate.exchange(nsiApiUrl + VERSION_CHECK_URL, HttpMethod.GET, HttpEntity.EMPTY, String.class, regionCode, serviceCode);
            if (response.getStatusCode().is2xxSuccessful()) return 3;
        } catch (EntityNotFoundException ex) {
            return 2;
        }
        throw new FormBaseWorkflowException("Ошибка обращения к сервису /api/nsi/v1/epgu/region");
    }

    @Override
    public EpguRegionResponse getRegion(String regionCode, String serviceCode, int timeout) {
        RestTemplate restTemplate = RestTemplateCreator.createOrDefault(restTemplateBuilder, timeout, defaultRestTemplate);
        try {
            ResponseEntity<EpguRegionResponse> response = restTemplate.exchange(nsiApiUrl + VERSION_CHECK_URL, HttpMethod.GET,
                    HttpEntity.EMPTY, EpguRegionResponse.class, regionCode, serviceCode);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().getRoutingCode() != null) {
                return response.getBody();
            }
        } catch (ExternalServiceException | RestClientException | EntityNotFoundException ex) {
            throw new FormBaseWorkflowException("Ошибка обращения к сервису /api/nsi/v1/epgu/region", ex);
        }
        throw new FormBaseWorkflowException("Ошибка обращения к сервису /api/nsi/v1/epgu/region");
    }
}
