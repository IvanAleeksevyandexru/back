package ru.gosuslugi.pgu.fs.service;

import ru.gosuslugi.pgu.dto.lk.LkDataMessage;

import java.util.List;

public interface LkNotifierService {

    void updateOrderRegion(Long orderId, String regionOkato);

    void updateOrderRegionByAddress(Long orderId, String address);

    void sendMessage(Long orderId, LkDataMessage message);

    void sendMessages(Long orderId, List<LkDataMessage> messages);
}
