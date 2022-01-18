package ru.gosuslugi.pgu.fs.pgu.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.core.exception.EntityNotFoundException;
import ru.gosuslugi.pgu.fs.pgu.dto.feed.UserTokenResponseDto;
import ru.gosuslugi.pgu.fs.pgu.service.UserTokenService;

import java.util.Map;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserTokenServiceImpl implements UserTokenService {

    private static final String LK_API_USER_TOKEN = "/lk-api/internal/api/lk/v1/users/offline/token?userId={userId}";

    @Value("${pgu.lkapi-url}")
    private String lkapiUrl;

    private final RestTemplate restTemplate;

    @Override
    public String getUserToken(Long userId) {
        if (Objects.isNull(userId)) {
            return null;
        }

        Map<String, Long> requestParams = Map.of(
                "userId", userId
        );

        try {
            UserTokenResponseDto response = restTemplate.getForObject(
                    lkapiUrl + LK_API_USER_TOKEN,
                    UserTokenResponseDto.class,
                    requestParams
            );
            String token = null;
            if (Objects.nonNull(response))
            {
                token = response.getToken();
            }
            return token;
        } catch (EntityNotFoundException e) {
            log.warn("Unable to find user token by userId {}", userId);
            return null;
        }
    }
}
