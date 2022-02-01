package ru.gosuslugi.pgu.fs.pgu.client.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.core.exception.EntityNotFoundException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.core.lk.model.feed.Feed;
import ru.gosuslugi.pgu.fs.common.utils.PguAuthHeadersUtil;
import ru.gosuslugi.pgu.fs.pgu.client.PguFeedClient;
import ru.gosuslugi.pgu.fs.pgu.dto.feed.FeedDto;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PguFeedClientImp implements PguFeedClient {

    @Value("${pgu.lkapi-url}")
    private String lkApiUrl;

    private static final String LK_API_FEEDS_PATH = "lk-api/api/lk/v1/feeds/{type}/{id}";

    private final RestTemplate restTemplate;
    private final UserPersonalData userPersonalData;

    @Override
    public FeedDto findFeed(Feed.FeedType type, Long id) {
        if (id == null) return null;

        Map<String, String> requestParams = Map.of(
                "type", type.name(),
                "id", String.valueOf(id)
        );

        try {
            ResponseEntity<FeedDto> response = restTemplate.exchange(
                    lkApiUrl + LK_API_FEEDS_PATH,
                    HttpMethod.GET,
                    new HttpEntity<>(PguAuthHeadersUtil.prepareAuthCookieHeaders(userPersonalData.getToken())),
                    FeedDto.class,
                    requestParams
            );
            return response.getBody();
        } catch (EntityNotFoundException e) {
            log.warn("Unable to find feed for type {} with id {}", type, id);
            return null;
        }
    }
}
