package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.fs.service.ConfirmEmailService;
import ru.gosuslugi.pgu.fs.common.utils.PguAuthHeadersUtil;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConfirmEmailServiceImpl implements ConfirmEmailService {

    private static final String SEND_CONFIRM_EMAIL_URL = "/api/lk/v1/orders/{orderId}/email/confirm/send";
    private static final String CHECK_CONFIRM_EMAIL_URL = "/api/lk/1/orders/{orderId}/sms/confirm/check?code={code}";

    @Value("${booking.lkapi-url}")
    private String pguUrl;
    private final UserPersonalData userPersonalData;
    private final RestTemplate restTemplate;

    @Override
    public HttpStatus sendConfirmationEmail(Long orderId) {
        try {
            ResponseEntity<Object> response = restTemplate.postForEntity(
                    pguUrl + SEND_CONFIRM_EMAIL_URL,
                    new HttpEntity<String>(PguAuthHeadersUtil.prepareAuthCookieHeaders(userPersonalData.getToken())),
                    Object.class,
                    Map.of("orderId", orderId)
            );
            return response.getStatusCode();
        } catch (ExternalServiceException ex) {
            return ex.getStatus();
        }
    }

    @Override
    public HttpStatus checkConfirmationEmail(String confirmationEmail, Long orderId) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    pguUrl + CHECK_CONFIRM_EMAIL_URL,
                    HttpMethod.GET,
                    new HttpEntity<String>(PguAuthHeadersUtil.prepareAuthCookieHeaders(userPersonalData.getToken())),
                    String.class,
                    Map.of("orderId", orderId,
                            "code", confirmationEmail)
            );
            return response.getStatusCode();
        } catch (ExternalServiceException ex) {
            return ex.getStatus();
        }
    }
}
