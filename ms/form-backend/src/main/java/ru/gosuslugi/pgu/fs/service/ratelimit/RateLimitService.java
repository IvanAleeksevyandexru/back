package ru.gosuslugi.pgu.fs.service.ratelimit;

import ru.gosuslugi.pgu.dto.ratelimit.RateLimitRequest;

public interface RateLimitService {

    String ERROR_MESSAGE_TEXT = "Ошибка при обращении к внешнему сервису";

    default void apiCheck(final String key) {
        apiCheck(new RateLimitRequest(), key, ERROR_MESSAGE_TEXT);
    }

    default void apiCheck(final String key, final String errorMessage) {
        apiCheck(new RateLimitRequest(), key, ERROR_MESSAGE_TEXT);
    }

    default void apiCheck(RateLimitRequest rateLimitRequest, final String key) {
        apiCheck(rateLimitRequest, key, ERROR_MESSAGE_TEXT);
    }

    default void apiCheck(RateLimitRequest rateLimitRequest, final String key, final String errorMessage) {
        apiCheck(key);
    }

}