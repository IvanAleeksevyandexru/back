package ru.gosuslugi.pgu.fs.common.service.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.common.core.exception.ValidationException;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.fs.common.component.BaseComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentRegistry;
import ru.gosuslugi.pgu.fs.common.service.AnswerValidationService;
import ru.gosuslugi.pgu.fs.common.service.ComponentService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.nonNull;

@Component
public class AnswerValidationServiceImpl implements AnswerValidationService {

    private final ComponentService componentService;
    private final ComponentRegistry componentRegistry;

    public AnswerValidationServiceImpl(ComponentService componentService, @Lazy ComponentRegistry componentRegistry) {
        this.componentService = componentService;
        this.componentRegistry = componentRegistry;
    }

    /**
     * Осуществляет валидацию ответов заявителя на текущем экране
     * @param scenarioDto сценарий
     * @param serviceDescriptor описание сценария
     * @return пары {@code <id_компонента, текст_ошибки>}, если валидация не пройдена, пустую {@code Map}, если
     * пройдена
     */
    @Override
    public ConcurrentHashMap<String, String> validateAnswers(ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        ConcurrentHashMap<String, String> incorrectAnswers = new ConcurrentHashMap<>();

        for (Map.Entry<String, ApplicantAnswer> answerEntry : Optional.ofNullable(scenarioDto.getCurrentValue()).orElse(new HashMap<>()).entrySet()) {
            Optional<FieldComponent> fieldComponent = componentService.getFieldComponent(scenarioDto.getServiceId(),answerEntry, serviceDescriptor);
            if(fieldComponent.isEmpty()) {
                new ValidationException("Cannot find fieldComponent for key " + answerEntry.getKey());
            }

            BaseComponent<?> component = componentRegistry.getComponent(fieldComponent.get().getType());
            if (nonNull(component)) {
                incorrectAnswers.putAll(component.validate(answerEntry, scenarioDto, fieldComponent.get()));
            }
        }
        return incorrectAnswers;
    }

    @Override
    public List<List<Map<String, String>>> validateItemsUniqueness(ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        List<List<Map<String, String>>> uniquenessErrors = new ArrayList<>();

        for (Map.Entry<String, ApplicantAnswer> answerEntry : Optional.ofNullable(scenarioDto.getCurrentValue()).orElse(new HashMap<>()).entrySet()) {
            Optional<FieldComponent> fieldComponent = componentService.getFieldComponent(scenarioDto.getServiceId(),answerEntry, serviceDescriptor);
            if(fieldComponent.isEmpty()) {
                new ValidationException("Cannot find fieldComponent for key " + answerEntry.getKey());
            }

            BaseComponent<?> component = componentRegistry.getComponent(fieldComponent.get().getType());
            if (nonNull(component)) {
                uniquenessErrors = component.validateItemsUniqueness(answerEntry, scenarioDto, fieldComponent.get());
            }
        }
        return uniquenessErrors;
    }

}
