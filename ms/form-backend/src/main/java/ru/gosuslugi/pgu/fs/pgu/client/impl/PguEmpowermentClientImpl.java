package ru.gosuslugi.pgu.fs.pgu.client.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.fs.pgu.client.PguEmpowermentClient;
import ru.gosuslugi.pgu.fs.pgu.dto.EmpowermentDto;
import ru.gosuslugi.pgu.fs.pgu.dto.EmpowermentListDto;
import ru.gosuslugi.pgu.fs.common.utils.PguAuthHeadersUtil;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.debug;
import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.error;

@Slf4j
@Component
@RequiredArgsConstructor
public class PguEmpowermentClientImpl implements PguEmpowermentClient {
    private static final String GET_ORG_POWERS_URL = "/esia-rs/api/public/v1/pow/orgs/{orgId}/prns/{oid}/powers";

    @Value("${pgu.empowerment-service-url}")
    private String url;

    private final RestTemplate restTemplate;

    @Override
    public Set<String> getUserEmpowerment(String token, Long oid, Long orgId) {
        debug(log, () -> String.format("Getting org powers for user %s and org %s", oid, orgId));
        Map<String, String> requestParameters = Map.of(
                "orgId", orgId.toString(),
                "oid", oid.toString()
        );
        try {
            ResponseEntity<EmpowermentListDto> response = restTemplate.exchange(url + GET_ORG_POWERS_URL,
                    HttpMethod.GET,
                    new HttpEntity<>(prepareHttpHeaders(token)),
                    EmpowermentListDto.class,
                    requestParameters
            );
            if (Objects.nonNull(response.getBody())
                    && Objects.nonNull(response.getBody().getElements())
                    && !response.getBody().getElements().isEmpty()) {
                return response.getBody().getElements().stream()
                        .map(EmpowermentDto::getMnemonic)
                        .collect(Collectors.toSet());
            }
        } catch (ExternalServiceException | RestClientException e) {
            error(log, () -> String.format("Error while receiving privileges form ESIA for user with id %s and org %s", oid, orgId), e);
        }
        return Collections.emptySet();
    }

    private HttpHeaders prepareHttpHeaders(String token) {
        HttpHeaders httpHeaders = PguAuthHeadersUtil.prepareAuthBearerHeaders(token);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return httpHeaders;
    }
}
