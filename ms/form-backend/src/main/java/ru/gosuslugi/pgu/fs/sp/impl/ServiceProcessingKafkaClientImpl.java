package ru.gosuslugi.pgu.fs.sp.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.SpAdapterDto;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.fs.sp.ServiceProcessingClient;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@AllArgsConstructor
@ConditionalOnProperty(prefix = "spring.kafka.producer.sp-adapter-batch", name = "enabled")
public class ServiceProcessingKafkaClientImpl implements ServiceProcessingClient {

    private final KafkaTemplate<Long, List<SpAdapterDto>> batchKafkaTemplate;

    @Override
    public void send(SpAdapterDto spAdapterDto, String targetTopic) {
        if (Objects.isNull(targetTopic)) {
            targetTopic = batchKafkaTemplate.getDefaultTopic();
        }
        try {
            batchKafkaTemplate.send(targetTopic, spAdapterDto.getOrderId(), List.of(spAdapterDto)).get();
            log.info("Отправлено в SpAdapter: {}", spAdapterDto);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Ошибка отправки в SpAdapter: {}", spAdapterDto, e);
            throw new FormBaseException("Ошибка отправки сообщения в SpAdapter", e);
        }

    }

    @Override
    public void sendSigned(SpAdapterDto spAdapterDto, String targetTopic) {
        spAdapterDto.setSigned(true);
        send(spAdapterDto, targetTopic);
    }

}
