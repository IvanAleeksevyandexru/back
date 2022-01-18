package ru.gosuslugi.pgu.fs.delirium.client;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.fs.delirium.DeliriumClient;
import ru.gosuslugi.pgu.fs.delirium.configuration.DeliriumClientConfiguration;
import ru.gosuslugi.pgu.fs.delirium.model.DeliriumResponseDto;
import ru.gosuslugi.pgu.fs.delirium.model.DeliriumStageDto;
import ru.gosuslugi.pgu.fs.delirium.model.DeliriumRequestDto;

@Slf4j
@AllArgsConstructor
public class DeliriumClientImpl implements DeliriumClient {

    private final static String PROCESSING_API_URL = "api/delirium/2/services/epgu2service/processing";
    private final static String STAGE_API_URL = "/internal/api/delirium/2/epgu2service/stages/{orderId}";
    private final static String CALC_STAGE_API_URL = "/internal/api/delirium/2/epgu2service/stages/calc/{orderId}";
    private final static String CALC_STAGE_DRAFT_API_URL = "/internal/api/delirium/2/epgu2service/stages/calcWithDraft/{orderId}";

    private final RestTemplate restTemplate;
    private final DeliriumClientConfiguration deliriumClientConfiguration;

    @Override
    public DeliriumResponseDto postOrder(DeliriumRequestDto deliriumRequestDto) {
        if (log.isDebugEnabled()) log.debug("Send draft with orderId = {}", deliriumRequestDto.getOrderId());
        DeliriumResponseDto deliriumResponseDto = new DeliriumResponseDto();
        String url = buildUrl(deliriumClientConfiguration.getUrl(), PROCESSING_API_URL);
        try {
            ResponseEntity<Object> response = restTemplate.postForEntity(url, deliriumRequestDto, Object.class);
            if (log.isDebugEnabled()) {
                log.debug("Response from delirium = {}; for orderId = {};", response.getStatusCodeValue(), deliriumRequestDto.getOrderId());
            }
            deliriumResponseDto.setStatus(response.getStatusCodeValue());
            return deliriumResponseDto;
        } catch (RestClientException e) {
            throw new ExternalServiceException(e);
        }
    }

    @Override
    public DeliriumStageDto getStage(Long orderId) {
        String url = buildUrl(deliriumClientConfiguration.getUrl(), STAGE_API_URL);
        try {
            return restTemplate.getForObject(url, DeliriumStageDto.class, orderId);
        } catch (RestClientException e) {
            throw new ExternalServiceException(e);
        }
    }

    @Override
    public DeliriumStageDto calcStage(Long orderId) {
        String url = buildUrl(deliriumClientConfiguration.getUrl(), CALC_STAGE_API_URL);
        try {
            return restTemplate.getForObject(url, DeliriumStageDto.class, orderId);
        } catch(RestClientException e) {
            throw new ExternalServiceException(e);
        }
    }

    @Override
    public DeliriumStageDto calcStageWithDraft(Long orderId) {
        String url = buildUrl(deliriumClientConfiguration.getUrl(), CALC_STAGE_DRAFT_API_URL);
        try {
            return restTemplate.getForObject(url, DeliriumStageDto.class, orderId);
        } catch(RestClientException e) {
            throw new ExternalServiceException(e);
        }
    }

    private String buildUrl(String... args) {
        return String.join("/", args);
    }
}
