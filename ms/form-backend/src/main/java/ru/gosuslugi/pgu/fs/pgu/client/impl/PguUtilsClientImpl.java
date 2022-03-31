package ru.gosuslugi.pgu.fs.pgu.client.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.atc.carcass.common.exception.FaultException;
import ru.gosuslugi.pgu.common.core.exception.EntityNotFoundException;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.fs.common.utils.PguAuthHeadersUtil;
import ru.gosuslugi.pgu.fs.pgu.client.PguUtilsClient;
import ru.gosuslugi.pgu.fs.pgu.dto.PguServiceCodes;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class PguUtilsClientImpl implements PguUtilsClient {

    @Value("${pgu.lkapi-url}")
    private String lkApiUrl;

    @Value("${pgu.catalog-url}")
    private String catalogUrl;

    private static final String LK_API_GET_PASSPORT_AND_TARGET_CODES = "/api/catalog/v3/services/{targetCode}_{serviceCode}/convert/new?platform=EPGU_V4";
    private static final String LK_API_USER_AUTHORITY_CHECK = "/api/lk/v1/users/data/authority/{authorityId}?extId={targetId}";

    private final RestTemplate restTemplate;
    private final UserPersonalData userPersonalData;

    @Override
    public boolean checkAuthorityForService(String authorityId, String targetId) {
        if(Objects.isNull(authorityId)){ return false; }
        try {
            restTemplate.exchange(
                    lkApiUrl + LK_API_USER_AUTHORITY_CHECK,
                    HttpMethod.GET,
                    new HttpEntity<>(PguAuthHeadersUtil.prepareAuthCookieHeaders(userPersonalData.getToken())),
                    Object.class,
                    Map.of(
                            "authorityId", authorityId,
                            "targetId", targetId
                    )
            );
        } catch (EntityNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    public PguServiceCodes getPguServiceCodes(String serviceCode, String targetCode) {
        try {
            var response = restTemplate.exchange(
                    catalogUrl + LK_API_GET_PASSPORT_AND_TARGET_CODES,
                    HttpMethod.GET,
                    new HttpEntity<>(PguAuthHeadersUtil.prepareAuthCookieHeaders(userPersonalData.getToken())),
                    PguServiceCodes.class,
                    Map.of(
                            "targetCode", targetCode,
                            "serviceCode", serviceCode
                    )
            );
            return response.getBody();
        } catch (ExternalServiceException | EntityNotFoundException e) {
            throw new FaultException("Ошибка при получении passport и target кодов. "  + e.getMessage(), e);
        }
    }
}
