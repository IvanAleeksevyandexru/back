package ru.gosuslugi.pgu.fs.action.impl;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.fs.common.exception.NoRightsForSendingApplicationException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.action.ActionRequestDto;
import ru.gosuslugi.pgu.dto.action.ActionResponseDto;
import ru.gosuslugi.pgu.fs.action.ActionService;
import ru.gosuslugi.pgu.fs.action.ActionType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DownloadTsReportPdfAction implements ActionService {

    private final RestTemplate restTemplate;
    private final UserPersonalData userPersonalData;

    @Value("${sp.integration.url:#{null}}")
    private String spUrl;

    @Value("${sp.integration.enabled}")
    private Boolean spEnabled;

    @Override
    public ActionType getActionType() {
        return ActionType.downloadTsReportPdf;
    }

    @Override
    public ActionResponseDto invoke(ActionRequestDto actionRequestDto) {
        var orderId = actionRequestDto.getScenarioDto().getOrderId();

        if (Strings.isNotBlank(spUrl) && spEnabled) {
            try {
                var headers = new HttpHeaders();
                headers.add(HttpHeaders.COOKIE, "acc_t=" + userPersonalData.getToken());
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                var entity = new HttpEntity<>(headers);
                ResponseEntity<byte[]> response = restTemplate.exchange(spUrl + "pdf?orderId={orderId}&pdfName=pdf&light=true",
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<>() {},
                        Map.of("orderId", orderId));

                return getResponse(response.getBody());
            } catch (RestClientException e) {
                throw new ExternalServiceException(e);
            } catch (ExternalServiceException e) {
                if (HttpStatus.FORBIDDEN.equals(e.getStatus())) {
                    throw new NoRightsForSendingApplicationException("Недостаточно прав для скачивания", e);
                }
                throw e;
            }
        }
        return null;
    }

    private ActionResponseDto getResponse(byte[] value){
        var response = new ActionResponseDto();
        Map<String, Object> responseParams = new HashMap<>();
        responseParams.put("value", value);
        responseParams.put("type", "application/pdf;base64");
        response.setResponseData(responseParams);
        return response;
    }
}
