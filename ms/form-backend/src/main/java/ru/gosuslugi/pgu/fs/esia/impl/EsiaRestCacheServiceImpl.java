package ru.gosuslugi.pgu.fs.esia.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.atc.carcass.security.model.EsiaOAuthTokenSession;
import ru.atc.carcass.security.rest.model.EsiaResponse;
import ru.atc.carcass.security.rest.model.EsiaUserId;
import ru.atc.carcass.security.rest.model.person.PersonContainer;
import ru.atc.carcass.security.service.impl.EsiaRestClientServiceImpl;
import ru.atc.carcass.security.service.impl.OAuthTokenUtil;
import ru.atc.carcass.security.service.impl.ThreadLocalTokensContainerManagerService;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.common.logging.service.SpanService;
import ru.gosuslugi.pgu.fs.config.properties.EsiaServiceProperties;
import ru.gosuslugi.pgu.fs.esia.EsiaCacheService;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class EsiaRestCacheServiceImpl implements EsiaCacheService {

    private final RestTemplate restTemplate;
    private final SpanService spanService;
    private final UserPersonalData userPersonalData;
    private final EsiaRestClientServiceImpl esiaRestClientService;
    private final EsiaServiceProperties esiaServiceProperties;
    private final ThreadLocalTokensContainerManagerService threadLocalTokensContainerManagerService;

    @Value("${esia.cache}")
    private String esiaCacheUrl;

    private static final String CLEAR_CACHE_URL = "/pso/change/prns?oid={userId}&opType={opType}";


    public void clearCache(Long userId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "acc_t=" + token);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(Collections.emptyMap(), headers);
        try {
            restTemplate.postForEntity(esiaCacheUrl + CLEAR_CACHE_URL,
                    entity,
                    Object.class,
                    Map.of(
                            "userId", userId,
                            "opType", "clear"
                    )
            );
            forceUpdateUserPersonalData(token);
        } catch (RestClientException e) {
            throw new ExternalServiceException(e);
        }
    }

    private void forceUpdateUserPersonalData(String token){
        log.warn("Force update of user data");
        EsiaOAuthTokenSession tokenInfo = OAuthTokenUtil.getTokenInfo(token);
        EsiaUserId esiaUserId = new EsiaUserId(tokenInfo.getUserId(), tokenInfo.getOrgOid(), null);
        threadLocalTokensContainerManagerService.put(esiaUserId, token);

        EsiaResponse esiaResponse = spanService.runExternalService(
                "esiaRestClientService: get all person data",
                "esia.request.getPersonAll",
                () -> esiaRestClientService.getPerson(esiaUserId,
                        Set.of("documents", "documents.elements", "kids", "addresses", "contacts", "contacts.elements")),
                Map.of("esiaUserId", String.valueOf(esiaUserId),
                        "url", esiaServiceProperties.getCalculatedUrl())
        );
        PersonContainer personContainer = (PersonContainer) esiaResponse;
        userPersonalData.update(personContainer, tokenInfo);
    }
}
