package ru.gosuslugi.pgu.fs.sp;

import ru.gosuslugi.pgu.dto.SpAdapterDto;

public interface ServiceProcessingClient {

    void send(SpAdapterDto spAdapterDto, String targetTopic);

    void sendSigned(SpAdapterDto spAdapterDto, String targetTopic);
}
