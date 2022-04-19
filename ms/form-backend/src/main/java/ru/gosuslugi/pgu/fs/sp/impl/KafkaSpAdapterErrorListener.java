package ru.gosuslugi.pgu.fs.sp.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.atc.carcass.common.exception.FaultException;
import ru.gosuslugi.pgu.common.core.exception.RetryKafkaException;
import ru.gosuslugi.pgu.common.kafka.properties.KafkaConsumerProperties;
import ru.gosuslugi.pgu.common.kafka.service.AbstractMessageListener;
import ru.gosuslugi.pgu.common.kafka.service.KafkaRetryService;
import ru.gosuslugi.pgu.common.logging.service.SpanService;
import ru.gosuslugi.pgu.dto.SpAdapterDto;
import ru.gosuslugi.pgu.dto.SpRequestErrorDto;
import ru.gosuslugi.pgu.fs.pgu.client.impl.OrderStatuses;
import ru.gosuslugi.pgu.fs.pgu.service.OrderAttributesService;
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService;

import java.util.List;

@Slf4j
@Component
@ConditionalOnExpression("${spring.kafka.producer.sp-adapter-batch.enabled}")
public class KafkaSpAdapterErrorListener extends AbstractMessageListener<Long, SpRequestErrorDto> {

    private final PguOrderService pguOrderService;
    private final OrderAttributesService orderAttributesService;

    public KafkaSpAdapterErrorListener(KafkaConsumerProperties spAdapterErrorConsumerProperties,
                                       KafkaRetryService kafkaRetryService,
                                       SpanService spanService,
                                       PguOrderService pguOrderService,
                                       OrderAttributesService orderAttributesService
    ) {
        super(spAdapterErrorConsumerProperties, kafkaRetryService, spanService);
        this.pguOrderService = pguOrderService;
        this.orderAttributesService = orderAttributesService;
    }


    @Override
    protected void processMessage(SpRequestErrorDto message) {
        try {
            var order = pguOrderService.findOrderByIdAndUserId(
                    message.getAdapterRequestDto().getOrderId(),
                    message.getAdapterRequestDto().getOid()
            );
            if (order.getOrderStatusId() == OrderStatuses.REGISTERED_ON_PORTAL.getStatusId()) {
                orderAttributesService.updateTechOrderStatusNoCache(
                        OrderStatuses.ERROR_SEND_REQUEST.getStatusId(),
                        message.getAdapterRequestDto().getOrderId()
                );
            }
        } catch (FaultException e) {
            throw new RetryKafkaException(e);
        } catch (Exception e) {
            log.error("Error on handling kafka message spRequestErrorDto: {}", message, e);
        }
    }

}
