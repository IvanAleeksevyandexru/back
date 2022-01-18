package ru.gosuslugi.pgu.fs.transformation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.draft.model.DraftHolderDto;
import ru.gosuslugi.pgu.dto.descriptor.transformation.TransformationOperation;
import ru.gosuslugi.pgu.fs.pgu.client.impl.OrderStatuses;
import ru.gosuslugi.pgu.fs.pgu.service.PguOrderService;
import ru.gosuslugi.pgu.fs.transformation.Transformation;
import ru.gosuslugi.pgu.fs.transformation.TransformationResult;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReuseOrderTransformation implements Transformation {

    private final PguOrderService pguOrderService;

    @Override
    public TransformationOperation getOperation() {
        return TransformationOperation.reuseOrder;
    }

    @Override
    public TransformationResult transform(DraftHolderDto current, Order order, Map<String, Object> spec) {
        pguOrderService.setTechStatusToOrder(order.getId(), OrderStatuses.DRAFT.getStatusId());
        return new TransformationResult(true, current, order);
    }
}
