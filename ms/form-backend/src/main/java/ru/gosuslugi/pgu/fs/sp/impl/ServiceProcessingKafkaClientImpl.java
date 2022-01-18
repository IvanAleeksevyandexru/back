package ru.gosuslugi.pgu.fs.sp.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.SpAdapterDto;
import ru.gosuslugi.pgu.dto.SpRequestErrorDto;
import ru.gosuslugi.pgu.dto.SpResponseOkDto;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.fs.pgu.client.impl.OrderStatuses;
import ru.gosuslugi.pgu.fs.pgu.service.OrderAttributesService;
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService;
import ru.gosuslugi.pgu.fs.service.ExecutableComponentsService;
import ru.gosuslugi.pgu.fs.sp.ServiceProcessingClient;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@AllArgsConstructor
@ConditionalOnExpression("${spring.kafka.producer.sp-adapter.enabled} || ${spring.kafka.producer.sp-adapter-batch.enabled}")
public class ServiceProcessingKafkaClientImpl implements ServiceProcessingClient {

    private final Optional<KafkaTemplate<Long, SpAdapterDto>> kafkaTemplate;
    private final Optional<KafkaTemplate<Long, List<SpAdapterDto>>> batchKafkaTemplate;
    private final PguOrderService pguOrderService;
    private final OrderAttributesService orderAttributesService;
    private final ExecutableComponentsService executableComponentsService;

    @Override
    public void send(SpAdapterDto spAdapterDto, String targetTopic) {
        sendWithTemplate(spAdapterDto.getOrderId(), spAdapterDto, kafkaTemplate, targetTopic);
        sendWithTemplate(spAdapterDto.getOrderId(), List.of(spAdapterDto), batchKafkaTemplate, targetTopic);
    }

    private <T> void sendWithTemplate(Long key, T value, Optional<KafkaTemplate<Long, T>> template, String targetTopic) {
        if (template.isEmpty()) {
            return;
        }

        val t = template.get();
        if (Objects.isNull(targetTopic)) {
            targetTopic = t.getDefaultTopic();
        }
        try {
            t.send(targetTopic, key, value).get();
            log.info("Отправлено в SpAdapter: {}", value);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Ошибка отправки в SpAdapter: {}", value, e);
            throw new FormBaseException("Ошибка отправки сообщения в SpAdapter", e);
        }

    }

    @KafkaListener(
            topics = "#{spAdapterConsumerProps.responseTopicName}",
            containerFactory = "spResponseListenerContainerFactory"
    )
    public void processOkFromSp(SpResponseOkDto spResponseOkDto) {
        try {
            log.info("Получено сообщение об успешной отправке завяления из SpAdapter: {}", spResponseOkDto);
            executableComponentsService.process(spResponseOkDto.getServiceId(), spResponseOkDto.getOrderId(), spResponseOkDto.getUserId(), null);
            deleteFromPguOrderService(spResponseOkDto);
        } catch (Exception e) {
            log.error("Error on handling kafka message spResponseOkDto: {}", spResponseOkDto, e);
        }
    }

    @KafkaListener(
            topics = "#{spAdapterConsumerProps.errorTopicName}",
            containerFactory = "spRequestErrorKafkaListenerContainerFactory"
    )
    public void processErrorFromSp(SpRequestErrorDto spRequestErrorDto) {
        try {
            log.info("Получено сообщение об ошибке формирования заявления из SpAdapter: {}", spRequestErrorDto);
            var order = pguOrderService.findOrderByIdAndUserId(
                    spRequestErrorDto.getAdapterRequestDto().getOrderId(),
                    spRequestErrorDto.getAdapterRequestDto().getOid()
            );
            if(order.getOrderStatusId() == OrderStatuses.REGISTERED_ON_PORTAL.getStatusId()){
                orderAttributesService.updateTechOrderStatus(
                        OrderStatuses.ERROR_SEND_REQUEST.getStatusId(),
                        spRequestErrorDto.getAdapterRequestDto().getOrderId()
                );
            }
        } catch (Exception e) {
            log.error("Error on handling kafka message spRequestErrorDto: {}", spRequestErrorDto, e);
        }
    }

    @Override
    public void sendSigned(SpAdapterDto spAdapterDto, String targetTopic) {
        spAdapterDto.setSigned(true);
        send(spAdapterDto, targetTopic);
    }

    private void deleteFromPguOrderService(SpResponseOkDto spResponseOkDto) {
        pguOrderService.findDraftsLight(spResponseOkDto.getServiceId(), spResponseOkDto.getTargetId(), spResponseOkDto.getUserId())
                .forEach(draft -> pguOrderService.deleteOrderByIdAndUserId(draft.getOrderId(), spResponseOkDto.getUserId()));
    }
}
