package ru.gosuslugi.pgu.fs.transformation.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.core.lk.model.order.Order;
import ru.gosuslugi.pgu.draft.model.DraftHolderDto;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.transformation.TransformationOperation;
import ru.gosuslugi.pgu.fs.common.descriptor.MainDescriptorService;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.transformation.Transformation;
import ru.gosuslugi.pgu.fs.transformation.TransformationResult;

import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.apache.commons.lang.StringUtils.isBlank;

@Slf4j
@Component
@AllArgsConstructor
public class TransformationFallbackToScreen implements Transformation {

    private static final String SCREEN_KEY = "screen";

    private final JsonProcessingService jsonProcessingService;
    private final MainDescriptorService mainDescriptorService;

    @Override
    public TransformationOperation getOperation() {
        return TransformationOperation.fallbackToScreen;
    }

    @Override
    public TransformationResult transform(DraftHolderDto draftOrigin, Order orderOrigin, Map<String, Object> spec) {
        String screen = Optional.ofNullable(spec).map(map -> map.get(SCREEN_KEY)).map(Objects::toString).orElse(null);
        if (isBlank(screen)) {
            if (log.isWarnEnabled()) {
                log.warn("Идентификатор экрана не указан для операции \"{}\" для черновика id = {}", getOperation(),  draftOrigin.getOrderId());
            }
            return new TransformationResult(false, draftOrigin, orderOrigin);
        }
        // Copping
        DraftHolderDto current = jsonProcessingService.clone(draftOrigin);
        ScenarioDto currentScenarioDto = current.getBody();
        LinkedList<String> screens = currentScenarioDto.getFinishedAndCurrentScreens();
        if (!screens.contains(screen)) {
            if (log.isWarnEnabled()) {
                log.warn("Идентификатор экрана \"{}\" отсутствует в списке {} пройденных экранов для операции \"{}\" для черновика id = {}", screen, screens, getOperation(),  draftOrigin.getOrderId());
            }
            return new TransformationResult(false, draftOrigin, orderOrigin);
        }
        ServiceDescriptor serviceDescriptor = mainDescriptorService.getServiceDescriptor(currentScenarioDto.getServiceDescriptorId());
        // Удаляем все экраны с конца, пока не встретим искомый
        while (!screens.get(screens.size() - 1).equals(screen)) {
            String lastScreen = screens.removeLast();
            removeAnswersForScreen(lastScreen, currentScenarioDto, serviceDescriptor);
        }
        removeAnswersForScreen(screen, currentScenarioDto, serviceDescriptor);

        // Выставляем этот экран в дисплей
        currentScenarioDto.getDisplay().setId(screen);
        return new TransformationResult(true, current, orderOrigin);
    }

    private void removeAnswersForScreen(String screenId, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        Optional<ScreenDescriptor> screenDescriptorById = serviceDescriptor.getScreenDescriptorById(screenId);
        screenDescriptorById.ifPresent(screenDescriptor -> {
            screenDescriptor.getComponentIds().forEach(componentId -> {
                ApplicantAnswer answer = scenarioDto.getApplicantAnswers().get(componentId);
                if (answer != null) {
                    scenarioDto.getCachedAnswers().put(componentId, answer);
                    scenarioDto.getApplicantAnswers().remove(componentId);
                }
            });
        });
    }
}
