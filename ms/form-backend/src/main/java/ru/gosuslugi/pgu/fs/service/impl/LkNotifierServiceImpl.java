package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.dto.lk.LkDataMessage;
import ru.gosuslugi.pgu.fs.service.LkNotifierClient;
import ru.gosuslugi.pgu.fs.service.LkNotifierService;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDadataService;

import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
@Slf4j
public class LkNotifierServiceImpl implements LkNotifierService {

    private final static String EXECUTION_REGION_STR = "executionRegion";
    private final static String EXECUTION_DICTIONARY_STR = "executionDictionary";
    private final static String EXECUTION_REGION_FIELD_NAME_STR = "NewSFRegion";
    private final static String EXECUTION_DICTIONARY_FIELD_NAME_STR = "NewSFDictionary";
    private final static String OKATO_STR = "OKATO";

    private final LkNotifierClient lkNotifierClient;
    private final NsiDadataService nsiDadataService;
    @Value("${lk-notifier.integration.enabled: false}")
    private boolean integrationEnabled;

    @Async
    @Override
    public void updateOrderRegion(Long orderId, String okato) {
        if (StringUtils.hasText(okato) && integrationEnabled && Objects.nonNull(orderId)) {
            lkNotifierClient.sendMessages(orderId, createOkatoEvents(okato));
        }
    }

    @Async
    @Override
    public void updateOrderRegionByAddress(Long orderId, String address) {
        if (StringUtils.hasText(address) && integrationEnabled && Objects.nonNull(orderId)) {
            String addressOkato = nsiDadataService.getAddressOkato(address);
            if (StringUtils.hasText(addressOkato)) {
                lkNotifierClient.sendMessages(orderId, createOkatoEvents(addressOkato));
            }
        }
    }

    @Async
    @Override
    public void sendMessage(Long orderId, LkDataMessage message) {
        if (integrationEnabled && message != null) {
            lkNotifierClient.sendMessages(orderId, List.of(message));
        }
    }

    @Async
    @Override
    public void sendMessages(Long orderId, List<LkDataMessage> messages) {
        if (integrationEnabled && messages != null && !messages.isEmpty()) {
            lkNotifierClient.sendMessages(orderId, messages);
        }
    }

    protected List<LkDataMessage> createOkatoEvents(String okato) {
        return List.of(new LkDataMessage(EXECUTION_REGION_FIELD_NAME_STR, okato, EXECUTION_REGION_STR),
                new LkDataMessage(EXECUTION_DICTIONARY_FIELD_NAME_STR, OKATO_STR, EXECUTION_DICTIONARY_STR));
    }
}