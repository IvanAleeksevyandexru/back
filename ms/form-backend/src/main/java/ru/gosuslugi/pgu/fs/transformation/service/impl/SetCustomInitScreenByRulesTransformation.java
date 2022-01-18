package ru.gosuslugi.pgu.fs.transformation.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.draft.model.DraftHolderDto;
import ru.gosuslugi.pgu.dto.descriptor.ScreenRule;
import ru.gosuslugi.pgu.dto.descriptor.transformation.TransformationOperation;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.common.service.ScreenFinderService;
import ru.gosuslugi.pgu.fs.transformation.Transformation;
import ru.gosuslugi.pgu.fs.transformation.TransformationResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SetCustomInitScreenByRulesTransformation implements Transformation {

    private final ScreenFinderService screenFinderService;
    private final MainDescriptorService mainDescriptorService;

    @Override
    public TransformationOperation getOperation() {
        return TransformationOperation.setCustomInitScreenByRules;
    }

    @Override
    public TransformationResult transform(DraftHolderDto current, Order order, Map<String, Object> spec) {

        List<ScreenRule> rules = spec != null && spec.containsKey(MappingTransformation.TRANSFORMATION_RULES_ATTR_NAME)
            ? JsonProcessingUtil.getObjectMapper().convertValue(
                spec.get(MappingTransformation.TRANSFORMATION_RULES_ATTR_NAME),
                new TypeReference<>() {})
            : Collections.emptyList();

        val scenarioDto = current.getBody();
        val serviceDescriptor = mainDescriptorService.getServiceDescriptor(scenarioDto.getServiceDescriptorId());

        val nextScreen = screenFinderService
            .findScreenDescriptorByRulesOrEmpty(scenarioDto, serviceDescriptor, rules);
        if (nextScreen.isEmpty()) {
            return new TransformationResult(false, current, order);
        }

        scenarioDto.getDisplay().setId(nextScreen.get().getId());
        return new TransformationResult(true, current, order);
    }
}
