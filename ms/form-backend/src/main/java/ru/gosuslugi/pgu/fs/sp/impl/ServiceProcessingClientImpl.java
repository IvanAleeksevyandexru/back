package ru.gosuslugi.pgu.fs.sp.impl;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.dto.SpAdapterDto;
import ru.gosuslugi.pgu.fs.common.exception.NoRightsForSendingApplicationException;
import ru.gosuslugi.pgu.fs.sp.ServiceProcessingClient;

@Component
@RequiredArgsConstructor
@ConditionalOnExpression("!${spring.kafka.producer.sp-adapter.enabled} && !${spring.kafka.producer.sp-adapter-batch.enabled}")
public class ServiceProcessingClientImpl implements ServiceProcessingClient {

    private final RestTemplate restTemplate;

    @Value("${sp.integration.url:#{null}}")
    private String spUrl;

    @Value("${sp.integration.enabled}")
    private Boolean spEnabled;

    public void send(SpAdapterDto spAdapterDto, String topicName){
        if(Strings.isNotBlank(spUrl) && spEnabled) {
            try {
                restTemplate.exchange(
                        spUrl + "/sendSmevRequest",
                        HttpMethod.POST,
                        new HttpEntity<>(spAdapterDto),
                        SpAdapterDto.class
                );
            } catch (RestClientException e) {
                throw new ExternalServiceException(e);
            } catch (ExternalServiceException ex) {
                if (HttpStatus.FORBIDDEN.equals(ex.getStatus())) {
                    throw new NoRightsForSendingApplicationException("Недостаточно прав для подачи заявления", ex);
                }
                throw ex;
            }
        }
    }

    public void sendSigned(SpAdapterDto spAdapterDto, String topicName) {
        if(Strings.isNotBlank(spUrl) && spEnabled) {
            try {
                restTemplate.exchange(
                        spUrl + "/sendSignedSmevRequest",
                        HttpMethod.POST,
                        new HttpEntity<>(spAdapterDto),
                        SpAdapterDto.class
                );
            } catch (RestClientException e) {
                throw new ExternalServiceException(e);
            } catch (ExternalServiceException ex) {
                if (HttpStatus.FORBIDDEN.equals(ex.getStatus())) {
                    throw new NoRightsForSendingApplicationException("Недостаточно прав для подачи заявления", ex);
                }
                throw ex;
            }
        }
    }
}
