package ru.gosuslugi.pgu.fs.common.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Collections;

/**
 * Общий класс для создания HttpHeaders
 * для отправки запросов в системы ПГУ которые принимают токен
 * acc_t в качестве Http-заголовка
 */
public class PguAuthHeadersUtil {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_PREFIX = "Bearer ";

    /** Метод создания Authorization-заголовка для доступа к ресурсам ПГУ */
    public static HttpHeaders prepareAuthBearerHeaders(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(AUTHORIZATION_HEADER, AUTHORIZATION_PREFIX + token);
        return httpHeaders;
    }

    /** Метод создания Cookie-заголовка для доступа к ресурсам ПГУ */
    public static HttpHeaders prepareAuthCookieHeaders(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Cookie", "acc_t="+token);
        return httpHeaders;
    }

}
