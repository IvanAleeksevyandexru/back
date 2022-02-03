package ru.gosuslugi.pgu.fs.pgu.client.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.core.exception.EntityNotFoundException;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.fs.common.utils.PguAuthHeadersUtil;
import ru.gosuslugi.pgu.fs.pgu.client.PguMedicalBirthCertificatesClient;
import ru.gosuslugi.pgu.fs.pgu.dto.medicalBirthCertificate.MedicalBirthCertificate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.debug;
import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.error;

/**
 * Клиент получает мед свидетельства пользователя EPGUCORE-85920
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PguMedicalBirthCertificatesClientImpl implements PguMedicalBirthCertificatesClient {

    private static final String MEDICAL_BIRTH_CERT_URL = "digital/api/public/v1/pso/{oid}/doc/mdcl_brth_cert";

    @Value("${pgu.birth-certificate-url}")
    private String url;
    private final RestTemplate restTemplate;

    @Override
    public List<MedicalBirthCertificate> getMedicalBirthCertificates(String token, Long oid) {
        debug(log, () -> String.format("Getting medical birth certificates for user %s", oid));
        Map<String, String> requestParams = Map.of(
                "oid", Objects.toString(oid)
        );
        try {
            ResponseEntity<List<MedicalBirthCertificate>> response = restTemplate
                    .exchange(url + MEDICAL_BIRTH_CERT_URL,
                            HttpMethod.GET,
                            new HttpEntity<>(prepareHttpHeaders(token)),
                            new ParameterizedTypeReference<>() {},
                            requestParams
                    );
            if (Objects.nonNull(response.getBody())
                    && !response.getBody().isEmpty()) {
                return response.getBody();
            }
        } catch (ExternalServiceException | RestClientException | EntityNotFoundException e) {
            error(log, () -> String.format("Error while receiving mdcl_brth_cert form ESIA for user with id %s", oid), e);
        }
        return Collections.emptyList();
    }
    private HttpHeaders prepareHttpHeaders(String token) {
        HttpHeaders httpHeaders = PguAuthHeadersUtil.prepareAuthBearerHeaders(token);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return httpHeaders;
    }
}