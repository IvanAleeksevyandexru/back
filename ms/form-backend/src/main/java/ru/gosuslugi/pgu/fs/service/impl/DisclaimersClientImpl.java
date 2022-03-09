package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.PortalDisclaimer;
import ru.gosuslugi.pgu.fs.service.DisclaimersClient;
import ru.gosuslugi.pgu.fs.common.utils.PguAuthHeadersUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class DisclaimersClientImpl implements DisclaimersClient {
    private final static String DISCLAIMERS_PATH = "/api/cms/v1/disclaimers/passport/{passCode}/{epguCode}";
    private final RestTemplate restTemplate;
    private final UserPersonalData userPersonalData;
    private final String disclaimersUrl;

    @Override
    public List<PortalDisclaimer> getDisclaimers(String passCode, String targetCode) {
        try {
            ResponseEntity<List<PortalDisclaimer>> response = restTemplate.exchange(disclaimersUrl + DISCLAIMERS_PATH,
                    HttpMethod.GET,
                    new HttpEntity<>(PguAuthHeadersUtil.prepareAuthCookieHeaders(userPersonalData.getToken())),
                    new ParameterizedTypeReference<>() {
                    },
                    Map.of("passCode", passCode,
                            "epguCode", targetCode)
            );
            if (HttpStatus.OK.equals(response.getStatusCode())) {
                return response.getBody();
            }
        } catch (ExternalServiceException e) {
            log.error("Error from external service while getting disclaimers", e);
        }
        return Collections.emptyList();
    }
}
