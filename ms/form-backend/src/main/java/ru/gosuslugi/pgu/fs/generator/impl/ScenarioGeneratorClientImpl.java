package ru.gosuslugi.pgu.fs.generator.impl;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.fs.exception.ErrorScreenException;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.common.core.exception.dto.ExternalErrorInfo;
import ru.gosuslugi.pgu.dto.ScenarioGeneratorDto;
import ru.gosuslugi.pgu.fs.generator.ScenarioGeneratorClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ScenarioGeneratorClientImpl implements ScenarioGeneratorClient {

    private static final String GENERATE_SCENARIO_URL = "/v1/scenario/fines";
    private static final String GENERATE_ADDITIONAL_SCENARIO_URL = "/v1/scenario/fines/additional?serviceId={serviceId}";

    private final RestTemplate restTemplate;

    @Value("${service-generator.integration.url:#{null}}")
    private String url;

    @Value("${service-generator.integration.enabled}")
    private Boolean enabled;

    @Override
    public void generateScenario(ScenarioGeneratorDto dto){
        if(Strings.isNotBlank(url) && enabled) {
            try {
                restTemplate.exchange(
                        url + GENERATE_SCENARIO_URL,
                        HttpMethod.POST,
                        new HttpEntity<>(dto),
                        String.class
                );
            } catch (ExternalServiceException e) {
                if (e.getValue() == null) {
                    throw e;
                }
                throw new ErrorScreenException(
                        Integer.parseInt(String.valueOf(e.getValue())),
                        new ExternalErrorInfo("generateScenario", GENERATE_SCENARIO_URL, HttpMethod.POST, e.getMessage(), null)
                );
            }
        }
    }

    @Override
    public void generateAdditionalSteps(String serviceId){
        if(Strings.isNotBlank(url) && enabled) {
            Map<String, String> requestParams = Map.of(
                    "serviceId", serviceId
            );
            try {
                restTemplate.exchange(
                        url + GENERATE_ADDITIONAL_SCENARIO_URL,
                        HttpMethod.POST,
                        null,
                        String.class,
                        requestParams

                );
            } catch (ExternalServiceException e) {
                throw new ErrorScreenException(
                        Integer.parseInt(String.valueOf(e.getValue())),
                        new ExternalErrorInfo("generateScenario", GENERATE_ADDITIONAL_SCENARIO_URL, HttpMethod.POST, e.getMessage(), null)
                );
            }
        }
    }
}
