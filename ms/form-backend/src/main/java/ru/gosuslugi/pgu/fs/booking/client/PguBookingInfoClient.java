package ru.gosuslugi.pgu.fs.booking.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.fs.booking.dto.PguBookingDto;
import ru.gosuslugi.pgu.fs.pgu.dto.ListOrderResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class PguBookingInfoClient {

    private final RestTemplate restTemplate;
    private final static String LK_API_BOOKING_ORDER = "/lk-api/internal/api/lk/v1/orders/{orderId}/set/attributes";

    @Value("${booking.lkapi-url}")
    private String pguUrl;

    public void sendPguBookingInfo(Long orderId, PguBookingDto pguBookingDto) {

        var headers = new HttpHeaders();
        headers.add(CONTENT_TYPE, APPLICATION_JSON_VALUE);

        var entity = new HttpEntity<>(pguBookingDto, headers);
        var requestParameters = Map.of(
                "orderId", orderId
        );

        restTemplate.exchange(pguUrl + LK_API_BOOKING_ORDER,
                HttpMethod.POST,
                entity,
                Void.class,
                requestParameters
        );
        log.info("Успешно отправлена запись на прием для orderId=" + orderId);
    }

}
