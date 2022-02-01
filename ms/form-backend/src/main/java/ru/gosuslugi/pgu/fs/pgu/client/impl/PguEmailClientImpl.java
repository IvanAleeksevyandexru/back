package ru.gosuslugi.pgu.fs.pgu.client.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.core.lk.model.PersonIdentifier;
import ru.gosuslugi.pgu.fs.common.exception.ErrorModalException;
import ru.gosuslugi.pgu.fs.common.utils.PguAuthHeadersUtil;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalDescriptorService;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalView;
import ru.gosuslugi.pgu.fs.pgu.client.PguEmailClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PguEmailClientImpl implements PguEmailClient {

    private static final String SEND_INVITATION_URL = "api/lk/v1/orders/{orderId}}/invitations/inviteToSign/send";

    @Value("${pgu.order-url}")
    private String pguUrl;

    private final RestTemplate restTemplate;
    private final UserPersonalData userPersonalData;
    private final ErrorModalDescriptorService errorModalDescriptorService;

    @Override
    public Boolean sendEmailInvitationToParticipant(Long orderId, PersonIdentifier participant) {
        HttpEntity<List<PersonIdentifier>> entity = new HttpEntity<>(List.of(participant),
                PguAuthHeadersUtil.prepareAuthCookieHeaders(userPersonalData.getToken()));
        try {
            restTemplate.exchange(pguUrl + SEND_INVITATION_URL,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {},
                    Map.of("orderId", orderId));
            return Boolean.TRUE;
        } catch (ExternalServiceException ex) {
            log.error("Send invitation to participant error: {}", ex);
            throw new ErrorModalException(errorModalDescriptorService.getErrorModal(ErrorModalView.REPEATABLE_INVITATION), "Пока нельзя отправить");
        }
    }
}
