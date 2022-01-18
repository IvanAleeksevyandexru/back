package ru.gosuslugi.pgu.fs.transformation.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.draft.model.DraftHolderDto;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.transformation.TransformationOperation;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.transformation.Transformation;
import ru.gosuslugi.pgu.fs.transformation.TransformationResult;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class TransformationAdditionalInformation implements Transformation {

    private static final String SCREEN_KEY = "screen";

    private final MainDescriptorService mainDescriptorService;

    @Override
    public TransformationOperation getOperation() {
        return TransformationOperation.additionalInformation;
    }

    @Override
    public TransformationResult transform(DraftHolderDto draftOrigin, Order orderOrigin, Map<String, Object> spec) {
        String screenId = Optional.ofNullable(spec).map(map -> map.get(SCREEN_KEY)).map(Objects::toString).orElse(null);
        if (StringUtils.isBlank(screenId)) {
            if (log.isWarnEnabled()) {
                log.warn("Идентификатор экрана не указан для операции \"{}\" для черновика id = {}", getOperation(),  draftOrigin.getOrderId());
            }
            return new TransformationResult(false, draftOrigin, orderOrigin);
        }

        ScenarioDto currentScenarioDto = draftOrigin.getBody();
        ServiceDescriptor serviceDescriptor = mainDescriptorService.getServiceDescriptor(currentScenarioDto.getServiceDescriptorId());
        if (serviceDescriptor.getScreenDescriptorById(screenId).isEmpty()) {
            if (log.isWarnEnabled()) {
                log.warn("Экрана с идентификатором {} не существует", screenId);
            }
            return new TransformationResult(false, draftOrigin, orderOrigin);
        }
        currentScenarioDto.getDisplay().setId(screenId);

        return new TransformationResult(true, draftOrigin, orderOrigin);
    }
}
