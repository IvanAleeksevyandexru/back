package ru.gosuslugi.pgu.fs.service.ratelimit.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.fs.config.properties.RateLimitProperties;
import ru.gosuslugi.pgu.dto.ratelimit.RateLimitRequest;
import ru.gosuslugi.pgu.common.core.exception.EntityNotFoundException;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.common.core.exception.RateLimitServiceException;
import ru.gosuslugi.pgu.fs.service.ratelimit.RateLimitService;

import java.util.Map;

/**
 * Интеграция с сервисом ratelimit
 * http://pgu-dev-fednlb.test.gosuslugi.ru/ratelimit-api/swagger-ui/#/Лимиты/checkLimitUsingGET
 *
 * Установка лимитов на использование услуги
 * https://jira.egovdev.ru/browse/EPGUCORE-71987
 */
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private final String VERSION = "v1";

    private final RestTemplate restTemplate;

    private final RateLimitProperties rateLimitProperties;

    private static final String API_PATH = "/api-check?key={key}&lim={limit}&ttl={ttl}";


    @Override
    public void apiCheck(RateLimitRequest rateLimitRequest, final String key, final String errorMessage) {

        try {
            // 200 -OK; 400 - Некорректный запрос; 429 - Превышен лимит; 500 - Внутренняя ошибка
            restTemplate.exchange(
                    rateLimitProperties.getPguUrl() + "/" + rateLimitRequest.getVersionOrDefault(VERSION) + API_PATH,
                    HttpMethod.GET,
                    null,
                    Void.class,
                    Map.of(
                            "key", key,
                            "limit", rateLimitRequest.getLimitOrDefault(rateLimitProperties.getLimit()),
                            "ttl", rateLimitRequest.getTtlOrDefault(rateLimitProperties.getTtl())
                    )
            );
        } catch (ExternalServiceException e) {
            if (e.getStatus() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new RateLimitServiceException(errorMessage, rateLimitRequest.getLimit(), e);
            }
            throw new ExternalServiceException(e);
        } catch (RestClientException | EntityNotFoundException e) {
            throw new ExternalServiceException(e);
        }
    }
}
