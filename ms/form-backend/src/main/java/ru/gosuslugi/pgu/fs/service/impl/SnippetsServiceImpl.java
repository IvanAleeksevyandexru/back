package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.fs.service.SnippetsClient;

import java.util.Map;

import static ru.gosuslugi.pgu.common.core.logger.LoggerUtil.error;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnippetsServiceImpl implements SnippetsClient {
    private static final String PATH = "lk-api/internal/api/lk/v1/feed/ORDER/{orderId}/custom-snippet";
    private final RestTemplate restTemplate;
    @Value("${pgu.lkapi-url}")
    private String endpoint;

    @Override
    public String setCustomSnippet(Long orderId, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        String snippetResult = "";
        try {
            var response = restTemplate.exchange(
                    endpoint + PATH, HttpMethod.POST, entity, String.class
                    , Map.of("orderId", orderId));
            if(response.getStatusCode().is2xxSuccessful()) {
                snippetResult = "Snippet is set";
            }
        }
        catch (Exception e) {
            error(log, () -> String.format("Error while setting custom snippet for orderId %s", orderId), e);
            snippetResult = ExceptionUtils.getRootCauseMessage(e);
        }
        return snippetResult;
    }
}
