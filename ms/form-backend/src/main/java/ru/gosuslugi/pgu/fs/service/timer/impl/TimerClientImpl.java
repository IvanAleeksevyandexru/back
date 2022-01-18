package ru.gosuslugi.pgu.fs.service.timer.impl;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.dto.PguTimer;
import ru.gosuslugi.pgu.fs.service.TimerClient;
import ru.gosuslugi.pgu.fs.service.timer.model.TimerRequestParameters;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @see <a href="http://pgu-dev-fednlb.test.gosuslugi.ru/timer-service/swagger-ui.html">Swagger</a>
 */
@AllArgsConstructor
public class TimerClientImpl implements TimerClient {
    private static final String BASE_TIMER_URL = "/api/timer-service/v1/{code}/{objectId}";
    private static final String OBJECT_ID_PARAM = "objectId";
    private static final String TIMER_CODE_PARAM = "code";
    private static final String UUID_PARAM = "uuid";
    private final RestTemplate restTemplate;

    private final String timerUrl;

    @Override
    public PguTimer getTimer(TimerRequestParameters parameters, String authToken) {
        String timerUUID = parameters.getUuid();
        String timerCode = parameters.getCode();
        String timerObjectId = Objects.requireNonNullElse(parameters.getObjectId(), timerUUID);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "acc_t=" + authToken);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(headers);
        Map<String, String> requestParameters = new HashMap<>(Map.of(
                OBJECT_ID_PARAM, timerObjectId,
                TIMER_CODE_PARAM, timerCode
        ));
        String url = timerUrl.concat(BASE_TIMER_URL);
        if(StringUtils.hasText(timerUUID)){
            url = url.concat("?" + UUID_PARAM + "={uuid}");
            requestParameters.put(UUID_PARAM, timerUUID);
        }
        try {
            ResponseEntity<PguTimer> response = restTemplate.exchange(url, HttpMethod.GET, entity, PguTimer.class, requestParameters);
            return response.getBody();
        } catch (RestClientException e) {
            throw new ExternalServiceException(e);
        }
    }
}
