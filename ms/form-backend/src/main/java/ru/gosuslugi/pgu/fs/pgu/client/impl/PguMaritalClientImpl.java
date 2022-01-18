package ru.gosuslugi.pgu.fs.pgu.client.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.core.exception.EntityNotFoundException;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.fs.pgu.client.PguMaritalClient;
import ru.gosuslugi.pgu.fs.pgu.dto.MaritalResponseItem;
import ru.gosuslugi.pgu.fs.common.utils.PguAuthHeadersUtil;
import java.util.*;
import java.util.stream.Collectors;
import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.error;

/**
 * Клиент получает сертификат о браке или о разводе
 * Возвращает лист сертификатов в зависимости от типа документа
 * Будет вызываться в новом компоненте MaritalStatusInput
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PguMaritalClientImpl implements PguMaritalClient {

    private static final String GET_MARRIED_CERT = "digital/api/public/v1/pso/{obj_oid}/doc/MARRIED_CERT";
    private static final String GET_DIVORCE_CERT = "digital/api/public/v1/pso/{obj_oid}/doc/DIVORCE_CERT";
    private static  final String MARRIED = "MARRIED_CERT";
    private static  final String DIVORCE = "DIVORCE_CERT";

    @Value("${pgu.marital-status-url}")
    private String url;
    private final RestTemplate restTemplate;

    @Override
    public List<MaritalResponseItem> getMaritalStatusCertificate(String token, Long oid, String documentType) {

        log.debug("Getting maritalstatus certificate for user %s" + oid);
        String endpoint = null;
        if (MARRIED.equals(documentType)) {
            endpoint = url + GET_MARRIED_CERT;
        }
        if (DIVORCE.equals(documentType)) {
            endpoint = url + GET_DIVORCE_CERT;
        }
        Map<String, String> requestParams = Map.of(
                "obj_oid",  Objects.toString(oid)
        );
        try {
            ResponseEntity<List<MaritalResponseItem>> response = restTemplate
                    .exchange(endpoint,
                            HttpMethod.GET,
                            new HttpEntity<>(prepareHttpHeaders(token)),
                            new ParameterizedTypeReference<>() {},
                            requestParams
                    );
            return  response.getBody().stream().map(maritalResponseItem ->
                    new MaritalResponseItem(
                            maritalResponseItem.getActNo(),maritalResponseItem.getActDate(),maritalResponseItem.getIssuedBy(),
                            maritalResponseItem.getSeries(), maritalResponseItem.getNumber(), maritalResponseItem.getIssueDate()))
                    .collect(Collectors.toList());
        } catch (ExternalServiceException | RestClientException | EntityNotFoundException e) {
            error(log, () -> String.format("Error while receiving maritalstatus certificate form ESIA for user with id %s", oid), e);
        }
        return  Collections.emptyList();
    }

    private HttpHeaders prepareHttpHeaders(String token) {
        HttpHeaders httpHeaders = PguAuthHeadersUtil.prepareAuthBearerHeaders(token);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return httpHeaders;
    }
}
