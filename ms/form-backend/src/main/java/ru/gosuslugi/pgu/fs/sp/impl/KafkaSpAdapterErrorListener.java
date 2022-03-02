package ru.gosuslugi.pgu.fs.sp.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.kafka.service.AbstractMessageListener;
import ru.gosuslugi.pgu.common.logging.service.SpanService;
import ru.gosuslugi.pgu.dto.SpRequestErrorDto;
import ru.gosuslugi.pgu.fs.pgu.client.impl.OrderStatuses;
import ru.gosuslugi.pgu.fs.pgu.service.OrderAttributesService;
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService;

@Slf4j
@Component
@ConditionalOnExpression("${spring.kafka.producer.sp-adapter-batch.enabled}")
public class KafkaSpAdapterErrorListener extends AbstractMessageListener<Long, SpRequestErrorDto> {

    private final PguOrderService pguOrderService;
    private final OrderAttributesService orderAttributesService;

    public KafkaSpAdapterErrorListener(SpanService spanService, PguOrderService pguOrderService, OrderAttributesService orderAttributesService) {
        super(spanService);
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
            if(order.getOrderStatusId() == OrderStatuses.REGISTERED_ON_PORTAL.getStatusId()){
                orderAttributesService.updateTechOrderStatusNoCache(
                        OrderStatuses.ERROR_SEND_REQUEST.getStatusId(),
                        message.getAdapterRequestDto().getOrderId()
                );
            }
        } catch (Exception e) {
            log.error("Error on handling kafka message spRequestErrorDto: {}", message, e);
        }
    }

}
