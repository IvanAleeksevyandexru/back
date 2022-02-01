package ru.gosuslugi.pgu.fs.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.StatusInfo;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.transformation.TransformationBlock;
import ru.gosuslugi.pgu.dto.descriptor.transformation.TransformationRule;
import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.draft.model.DraftHolderDto;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.service.TransformService;
import ru.gosuslugi.pgu.fs.transformation.TransformationResult;
import ru.gosuslugi.pgu.fs.transformation.service.impl.TransformationRegistry;
import ru.gosuslugi.pgu.fs.transformation.service.TransformationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransformServiceImpl implements TransformService {

    private final TransformationRegistry registry;
    private final MainDescriptorService mainDescriptorService;
    private final TransformationService transformationService;

    @Override
    public TransformationResult checkAndTransform(Order order, String serviceId, DraftHolderDto draftHolderDto) {
        StatusInfo statusInfo = new StatusInfo(order.getCurrentStatusHistoryId(), order.getOrderStatusId(), order.getUpdated());

        boolean wasNotTransformed = isNull(draftHolderDto.getBody().getStatuses()) || !draftHolderDto.getBody().getStatuses().contains(statusInfo);
        // Нужна трансформация: в сценарии есть код и мы еще не обрабатывали его
        if (isAcceptedCode(statusInfo.getStatusId(), serviceId) && wasNotTransformed) {
            Map<String, List<TransformationRule>> statusTransformationMap = mainDescriptorService.getServiceDescriptor(serviceId).getStatusTransformationRules();
            List<TransformationBlock> transformationBlocks = mainDescriptorService.getServiceDescriptor(serviceId).getTransformation();
            if(Objects.isNull(transformationBlocks)){
                transformationBlocks = new ArrayList<>();
            }

            if (Objects.nonNull(statusTransformationMap) && !statusTransformationMap.isEmpty()) {
                transformationBlocks.addAll(
                        statusTransformationMap
                                .entrySet()
                                .stream()
                                .map(entry -> {
                                    var transformBlock = new TransformationBlock();
                                    transformBlock.setStatusId(entry.getKey());
                                    transformBlock.setRules(entry.getValue());
                                    return transformBlock;
                                })
                                .collect(Collectors.toList()));
            }


            return transformationService.transform(
                    transformationBlocks
                            .stream()
                            .filter(e-> e
                                    .getStatusId()
                                    .equals(statusInfo.getStatusId().toString())
                            )
                            .findFirst()
                            .get(),
                    statusInfo,
                    order,
                    draftHolderDto
            );
        }
        return new TransformationResult(false, draftHolderDto, order);
    }

    @Override
    public boolean isAcceptedCode(Long statusCode, String serviceId) {
        ServiceDescriptor descriptor = mainDescriptorService.getServiceDescriptor(serviceId);
        Map<String, List<TransformationRule>> statusTransformationMap = descriptor.getStatusTransformationRules();
        var transformationSection = descriptor.getTransformation();
        if(Objects.isNull(transformationSection)){
            transformationSection = new ArrayList<>();
        }
        // TODO пока только для единоличного заполнителя
        return descriptor.checkOnlyOneApplicantAndStage()
                && !isNull(statusTransformationMap)
                && (statusTransformationMap.containsKey(statusCode.toString())
                || transformationSection.stream().anyMatch(e -> e.getStatusId().equals(statusCode.toString()))
        );
    }
}
