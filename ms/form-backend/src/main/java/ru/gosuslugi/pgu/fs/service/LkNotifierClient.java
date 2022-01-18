package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.dto.lk.LkDataMessage;

import java.util.List;

public interface LkNotifierClient {

    void sendMessages(Long orderId, List<LkDataMessage> lkDataMessages);

}
