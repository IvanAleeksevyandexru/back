package ru.gosuslugi.pgu.fs.sp.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import ru.atc.carcass.common.exception.FaultException;
import ru.gosuslugi.pgu.common.core.exception.ExternalServiceException;
import ru.gosuslugi.pgu.common.core.exception.RetryKafkaException;
import ru.gosuslugi.pgu.common.kafka.properties.KafkaConsumerProperties;
import ru.gosuslugi.pgu.common.kafka.service.AbstractMessageListener;
import ru.gosuslugi.pgu.common.kafka.service.KafkaRetryService;
import ru.gosuslugi.pgu.common.logging.service.SpanService;
import ru.gosuslugi.pgu.dto.SpResponseOkDto;
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService;
import ru.gosuslugi.pgu.fs.service.ExecutableComponentsService;

@Slf4j
@Component
@ConditionalOnExpression("${spring.kafka.producer.sp-adapter-batch.enabled}")
public class KafkaSpAdapterResponseListener extends AbstractMessageListener<Long, SpResponseOkDto> {

    private final ExecutableComponentsService executableComponentsService;
    private final PguOrderService pguOrderService;

    public KafkaSpAdapterResponseListener(KafkaConsumerProperties spAdapterResponseConsumerProperties,
                                          KafkaRetryService kafkaRetryService,
                                          SpanService spanService,
                                          ExecutableComponentsService executableComponentsService,
                                          PguOrderService pguOrderService) {
        super(spAdapterResponseConsumerProperties, kafkaRetryService, spanService);
        this.executableComponentsService = executableComponentsService;
        this.pguOrderService = pguOrderService;
    }

    @Override
    protected void processMessage(SpResponseOkDto message) {
        try {
            executableComponentsService.process(message.getServiceId(), message.getOrderId(), message.getUserId(), null);
            deleteFromPguOrderService(message);
        } catch (ExternalServiceException | FaultException e) {
            throw new RetryKafkaException(e);
        } catch (Exception e) {
            log.error("Error on handling kafka message spResponseOkDto: {}", message, e);
        }
    }

    private void deleteFromPguOrderService(SpResponseOkDto spResponseOkDto) {
        pguOrderService.findDraftsLight(spResponseOkDto.getServiceId(), spResponseOkDto.getTargetId(), spResponseOkDto.getUserId())
                .forEach(draft -> pguOrderService.deleteOrderByIdAndUserId(draft.getOrderId(), spResponseOkDto.getUserId()));
    }

}
