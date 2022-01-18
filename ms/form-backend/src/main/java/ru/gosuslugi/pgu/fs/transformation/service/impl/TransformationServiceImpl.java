package ru.gosuslugi.pgu.fs.transformation.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.StatusInfo;
import ru.gosuslugi.pgu.dto.descriptor.transformation.TransformationBlock;
import ru.gosuslugi.pgu.dto.descriptor.transformation.TransformationRule;
import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.draft.model.DraftHolderDto;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.transformation.Transformation;
import ru.gosuslugi.pgu.fs.transformation.TransformationResult;
import ru.gosuslugi.pgu.fs.transformation.service.TransformationService;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

@Slf4j
@Component
@AllArgsConstructor
public class TransformationServiceImpl implements TransformationService {

    private final JsonProcessingService jsonProcessingService;
    private final TransformationRegistry registry;

    @Override
    public TransformationResult transform(
        TransformationBlock transformationBlock,
        StatusInfo statusInfo,
        Order order,
        DraftHolderDto origin
    ) {
        // Copping
        DraftHolderDto currentDraft = jsonProcessingService.clone(origin);
        Order currentOrder = order.copy();
        currentOrder.setId(currentDraft.getOrderId());

        if (!isNull(transformationBlock.getRules())) {
            for (TransformationRule rule : transformationBlock.getRules()) {
                TransformationResult result = transform(rule, currentDraft, currentOrder);
                if (!result.isTransformed()) {
                    if (log.isWarnEnabled()) {
                        log.warn("Отказ от трансформации для черновика id = {}, так как оперрация \"{}\" не отработала", currentDraft.getOrderId(), rule.getOperation());
                    }
                    return new TransformationResult(false, origin, order);
                }
                currentDraft = result.getDraftHolderDto();
                currentOrder = result.getOrder();
            }
        }

        // Добавление отработанного статуса
        if (isNull(currentDraft.getBody().getStatuses())) {
           currentDraft.getBody().setStatuses(new ArrayList<>());
        }
        if(!transformationBlock.getOptions().isSkipHistory()){
            currentDraft.getBody().getStatuses().add(statusInfo);
            if (log.isInfoEnabled()) {
                log.info("Добавлен статус {} о проведенная трансформация для черновика id = {}", statusInfo, currentDraft.getOrderId());
            }
        }
        return new TransformationResult(true, currentDraft, currentOrder);
    }

    private TransformationResult transform(TransformationRule rule, DraftHolderDto currentDraft, Order currentOrder) {
        if (isNull(rule) || isNull(rule.getOperation())) {
            if (log.isWarnEnabled()) {
                log.warn("Не задана оперрация для черновика id = {}", currentDraft.getOrderId());
            }
            return new TransformationResult(false, currentDraft, currentOrder);
        }
        Transformation action = registry.getTransformation(rule.getOperation());
        if (isNull(action)) {
            if (log.isWarnEnabled()) {
                log.warn("Не найдена оперрация \"{}\" для черновика id = {}", rule.getOperation(), currentDraft.getOrderId());
            }
            return new TransformationResult(false, currentDraft, currentOrder);
        }
        return action.transform(currentDraft, currentOrder, rule.getSpec());
    }
}
