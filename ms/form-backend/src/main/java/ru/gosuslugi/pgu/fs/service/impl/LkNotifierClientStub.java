package ru.gosuslugi.pgu.fs.service.impl;

import ru.gosuslugi.pgu.dto.lk.LkDataMessage;
import ru.gosuslugi.pgu.fs.service.LkNotifierClient;

import java.util.List;


public class LkNotifierClientStub implements LkNotifierClient {

    @Override
    public void sendMessages(Long orderId, List<LkDataMessage> lkDataMessages) {
    }
}
