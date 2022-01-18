package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.dto.lk.LkDataMessage;
import ru.gosuslugi.pgu.dto.lk.SendNotificationRequestDto;
import ru.gosuslugi.pgu.fs.service.LkNotifierClient;

import java.util.List;

@RequiredArgsConstructor
public class LkNotifierClientImpl implements LkNotifierClient {

    private final RestTemplate restTemplate;

    private final String lkNotifierUrl;

    @Override
    public void sendMessages(Long orderId, List<LkDataMessage> lkDataMessages) {
        SendNotificationRequestDto request = new SendNotificationRequestDto(orderId, lkDataMessages);
        restTemplate.exchange(
                lkNotifierUrl + "/v1/notification/send",
                HttpMethod.POST,
                new HttpEntity<>(request),
                Void.class
        );
    }
}
