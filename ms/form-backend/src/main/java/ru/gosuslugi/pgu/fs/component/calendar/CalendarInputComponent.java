package ru.gosuslugi.pgu.fs.component.calendar;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.core.exception.JsonParsingException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.BaseComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentRegistry;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ru.gosuslugi.pgu.components.FieldComponentUtil.COMPONENTS_KEY;

@Slf4j
@RequiredArgsConstructor
@Component
@DependsOn("componentRegistry")
public class CalendarInputComponent extends AbstractComponent<String> {

    private final ComponentRegistry componentRegistry;

    @Override
    public ComponentType getType() {
        return ComponentType.CalendarInput;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        List<FieldComponent> componentsToShow = FieldComponentUtil.getFieldComponentsFromAttrs(component, serviceDescriptor);
        HashMap<String, Object> componentValuesById = new HashMap<>();
        List<FieldComponent> processedFieldComponents = new ArrayList<>();

        componentsToShow.forEach(internalFieldComponent -> {
            BaseComponent<?> internalComponent = componentRegistry.getComponent(internalFieldComponent.getType());
            if (Objects.nonNull(internalComponent)) {
                FieldComponent fieldComponentCopy = FieldComponent.getCopy(internalFieldComponent);

                // Выполняется для задействования LinkedValues во вложенных компонетах
                internalComponent.process(fieldComponentCopy, scenarioDto, serviceDescriptor);

                componentValuesById.put(
                        fieldComponentCopy.getId(),
                        AnswerUtil.tryParseToMap(fieldComponentCopy.getValue())
                );
                processedFieldComponents.add(fieldComponentCopy);
            } else {
                // задаем значения для компонентов которых нет на беке
                componentValuesById.put(
                        internalFieldComponent.getId(),
                        AnswerUtil.tryParseToMap(internalFieldComponent.getValue())
                );
                processedFieldComponents.add(internalFieldComponent);
            }
        });
        component.getAttrs().put(COMPONENTS_KEY, processedFieldComponents);
        return ComponentResponse.of(JsonProcessingUtil.toJson(componentValuesById));
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        Map<String, String> answerMap = new HashMap<>();

        try {
            answerMap.putAll(JsonProcessingUtil.fromJson(AnswerUtil.getValue(entry), new TypeReference<>() {}));
        } catch (JsonParsingException e) {
            log.error("В ответе пользователя отсутствует значение : {}", answerMap);
            incorrectAnswers.put(entry.getKey(), "Значение не задано");
            return;
        }

        FieldComponent displayedComponent = scenarioDto.getDisplay().getComponents().stream()
                .filter(f -> f.getId().equals(entry.getKey()))
                .findAny()
                .orElse(null);

        List<FieldComponent> fieldComponents = FieldComponentUtil.getChildrenList(displayedComponent);
        fieldComponents.forEach(component -> {
            BaseComponent<?> baseComponent = componentRegistry.getComponent(component.getType());
            incorrectAnswers.putAll(baseComponent.validate(
                    AnswerUtil.createAnswerEntry(component.getId(), answerMap.get(component.getId())),
                    scenarioDto,
                    component
            ));
        });
    }
}
