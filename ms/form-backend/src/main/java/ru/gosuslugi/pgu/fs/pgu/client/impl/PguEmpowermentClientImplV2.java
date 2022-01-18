package ru.gosuslugi.pgu.fs.pgu.client.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.fs.pgu.client.PguEmpowermentClientV2;
import ru.gosuslugi.pgu.fs.pgu.dto.EmpowermentDTOV2.AuthorityList;
import ru.gosuslugi.pgu.fs.pgu.dto.EmpowermentDTOV2.InnerElement;
import ru.gosuslugi.pgu.fs.pgu.dto.EmpowermentDTOV2.Power;
import ru.gosuslugi.pgu.fs.pgu.dto.EmpowermentListDTOV2;
import ru.gosuslugi.pgu.fs.common.utils.PguAuthHeadersUtil;

import java.util.*;
import java.util.stream.Collectors;

import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.debug;
import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.error;

@Slf4j
@Component
@RequiredArgsConstructor
public class PguEmpowermentClientImplV2 implements PguEmpowermentClientV2 {
    private static final String GET_POWER_URL = "esia-rs/api/public/v1/pow/obj/{obj_oid}/poweratt";

    @Value("${pgu.empowerment-service-url}")
    private String url;

    private final RestTemplate restTemplate;

    @Override
    public Set<String> getUserEmpowerment(String token, Long oid) {
        debug(log, () -> String.format("Getting org powers for user %s", oid));
        String endpoint = url + GET_POWER_URL;
        Map<String, String> requestParams = Map.of(
                "obj_oid", oid.toString()
        );

        try {
            ResponseEntity<EmpowermentListDTOV2> response = restTemplate.exchange(endpoint,
                    HttpMethod.GET,
                    new HttpEntity<>(prepareHttpHeaders(token)),
                    EmpowermentListDTOV2.class,
                    requestParams
            );
            if (Objects.nonNull(response.getBody())
                    && Objects.nonNull(response.getBody().getElements())
                    && !response.getBody().getElements().isEmpty()) {
                return response.getBody().getElements().stream()
                        .map(Power::getEmpowerments)
                        .map(AuthorityList::getElements)
                        .flatMap(Collection::stream)
                        .map(InnerElement::getMnemonic)
                        .collect(Collectors.toSet());
            }
        } catch (ExternalServiceException | RestClientException e) {
            error(log, () -> String.format("Error while receiving privileges form ESIA for user with id %s", oid), e);
        }
        return Collections.emptySet();
    }


    private HttpHeaders prepareHttpHeaders(String token) {
        HttpHeaders httpHeaders = PguAuthHeadersUtil.prepareAuthBearerHeaders(token);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return httpHeaders;
    }
}