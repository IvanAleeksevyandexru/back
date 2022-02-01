package ru.gosuslugi.pgu.fs.component.input;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ApplicantAnswerItem;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.*;
import ru.gosuslugi.pgu.fs.common.service.ListComponentItemUniquenessService;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.utils.ZagranpassportRepeatableFieldsValidationUtil;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static ru.gosuslugi.pgu.components.ComponentAttributes.SNILS;

@Component
@Slf4j
public class RepeatableFieldsComponent extends AbstractCycledComponent<String> {

    public static final String SNILS_JSON_NODE = "\"snils\"";

    private final ComponentRegistry componentRegistry;
    private final ListComponentItemUniquenessService listComponentItemUniquenessService;

    public RepeatableFieldsComponent(@Lazy ComponentRegistry componentRegistry,
                                     ListComponentItemUniquenessService listComponentItemUniquenessService) {
        this.componentRegistry = componentRegistry;
        this.listComponentItemUniquenessService = listComponentItemUniquenessService;
    }

    @Override
    public ComponentType getType() {
        return ComponentType.RepeatableFields;
    }

    @Override
    public void preProcess(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor currentDescriptor) {
        List<FieldComponent> componentsToShow = FieldComponentUtil.getFieldComponentsFromAttrs(component, currentDescriptor);
        if (!componentsToShow.isEmpty()) {
            component.getAttrs().put(FieldComponentUtil.COMPONENTS_KEY, componentsToShow);
        }
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        List<FieldComponent> internalFieldComponents = (List<FieldComponent>) component.getAttrs().get(FieldComponentUtil.COMPONENTS_KEY);
        List<Map<String, Object>> internalComponentsPreset = new ArrayList<>();
        HashMap<String, Object> componentValuesById = new HashMap<>();
        internalComponentsPreset.add(componentValuesById);
        List<FieldComponent> processedFieldComponents = new ArrayList<>();

        internalFieldComponents.forEach(internalFieldComponent -> {
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
        component.getAttrs().put(FieldComponentUtil.COMPONENTS_KEY, processedFieldComponents);

        listComponentItemUniquenessService.updateDisclaimerForUniquenessErrors(component, scenarioDto.getUniquenessErrors());

        return ComponentResponse.of(JsonProcessingUtil.toJson(internalComponentsPreset));
    }

    @Override
    public ComponentResponse<String> getCycledInitialValue(FieldComponent component, Map<String, Object> externalData) {
        List<FieldComponent> internalComponents = (List<FieldComponent>) component.getAttrs().get(FieldComponentUtil.COMPONENTS_KEY);
        List<Map<String, Object>> internalComponentsPreset = new ArrayList<>();
        internalComponentsPreset.add(new HashMap<>());
        internalComponents.forEach(fieldComponent -> {
            internalComponentsPreset.get(0).put(fieldComponent.getId(), fieldComponent.getValue());
        });
        return ComponentResponse.of(JsonProcessingUtil.toJson(internalComponentsPreset));
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        List<Map<String, String>> childrenAnswers = AnswerUtil.toStringMapList(entry, true);
        FieldComponent repeatableFieldsComponent = scenarioDto.getDisplay().getComponents().stream()
                .filter(f -> f.getId().equals(entry.getKey()))
                .findAny()
                .orElse(null);
        List<FieldComponent> childrenComponents = FieldComponentUtil.getChildrenList(repeatableFieldsComponent);

        List<Map<String, String>> incorrectChildrenAnswerList = validateForms(fieldComponent, scenarioDto, childrenAnswers, childrenComponents);

        if (incorrectChildrenAnswerList.stream().anyMatch(map -> !map.isEmpty())) {
            incorrectAnswers.put(entry.getKey(), JsonProcessingUtil.toJson(incorrectChildrenAnswerList));
        }
        if (incorrectAnswers.isEmpty()) {
            String name = entry.getKey();
            AnswerUtil.setCycleReferenceValue(scenarioDto, fieldComponent);

            Arrays.<Supplier<Map.Entry<String, String>>>asList(
                    () ->  ZagranpassportRepeatableFieldsValidationUtil.childNameChangeAllFieldsEqual(name, childrenAnswers, fieldComponent)
            ).forEach(
                    supplier -> {

                        // Если ошибок еще нет, делаем очередную проверку и добавляем ошибку при ненулевом результате
                        if (!incorrectAnswers.containsKey(name)) {
                            Optional.ofNullable(supplier.get()).ifPresent(pair -> incorrectAnswers.put(pair.getKey(), pair.getValue()));
                        }
                    }
            );
        }
    }

    private List<Map<String, String>> validateForms(
            FieldComponent fieldComponent,
            ScenarioDto scenarioDto,
            List<Map<String, String>> childrenAnswers,
            List<FieldComponent> childrenComponents
    ) {
        List<Map<String,String>> result = new ArrayList<>();
        for (int i = 0; i < childrenAnswers.size(); i++) {
            Map<String, String> childrenAnswerMap = childrenAnswers.get(i);
            List<FieldComponent> componentsForCurrentAnswer = childrenComponents.stream()
                    .filter(component -> childrenAnswerMap.containsKey(component.getId()))
                    .collect(Collectors.toList());
            // Validate form
            Map<String,String> incorrectAnswers = new HashMap<>();
            for (FieldComponent childrenComponent : componentsForCurrentAnswer) {
                Map.Entry<String, ApplicantAnswer> answerEntry = AnswerUtil.createRepeatableItemAnswerEntry(
                        childrenComponent.getId(), childrenAnswerMap.get(childrenComponent.getId()), i);

                // Validate by helpers
                BaseComponent<?> component = componentRegistry.getComponent(childrenComponent.getType());
                if (Objects.nonNull(component)) {
                    normalizeValueByType(childrenAnswerMap, childrenComponent, answerEntry, component);

                    incorrectAnswers.putAll(component.validate(answerEntry, scenarioDto, childrenComponent));
                    updateApplicantAnswer(fieldComponent, scenarioDto, answerEntry);
                }
            }
            result.add(incorrectAnswers);
        }
        return result;
    }

    private void normalizeValueByType(Map<String, String> childrenAnswerMap, FieldComponent childrenComponent, Map.Entry<String, ApplicantAnswer> answerEntry, BaseComponent<?> component) {
        if (ComponentType.SnilsInput.equals(component.getType()) && answerEntry.getValue().getValue().contains(SNILS_JSON_NODE)){
            Map<String, Object> nestedObject = jsonProcessingService.fromJson(answerEntry.getValue().getValue(), LinkedHashMap.class);
            if (Objects.nonNull(nestedObject.get(SNILS))){
                String nestedSnils = Objects.toString(nestedObject.get(SNILS));
                answerEntry.getValue().setValue(nestedSnils);
                childrenAnswerMap.put(childrenComponent.getId(), nestedSnils);
            }
        }
    }

    private void updateApplicantAnswer(FieldComponent fieldComponent,
                                       ScenarioDto scenarioDto,
                                       Map.Entry<String, ApplicantAnswer> answerEntry) {
        ApplicantAnswerItem applicantAnswerItem = (ApplicantAnswerItem) answerEntry.getValue();
        Optional.ofNullable(scenarioDto.getCurrentValue().get(fieldComponent.getId()))
                .ifPresent(applicantAnswer -> {
                    List<Map<Object, Object>> applicantAnswerMapList = AnswerUtil.toMapList(
                            AnswerUtil.createAnswerEntry(fieldComponent.getId(), applicantAnswer.getValue()),
                            true);
                    applicantAnswerMapList
                            .get(applicantAnswerItem.getIndex())
                            .put(answerEntry.getKey(), AnswerUtil.tryParseToMap(answerEntry.getValue().getValue()));
                    applicantAnswer.setValue(JsonProcessingUtil.toJson(applicantAnswerMapList));
                });
    }

    public List<List<Map<String, String>>> validateItemsUniqueness(Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        if ((Boolean) fieldComponent.getAttrs().getOrDefault(FieldComponentUtil.IS_CYCLED_KEY, Boolean.FALSE)) {
            return Collections.emptyList();
        }
        List<Map<String, String>> childrenAnswers = AnswerUtil.toStringMapList(entry, true);
        return listComponentItemUniquenessService.validateRepeatableFieldsItemsUniqueness(fieldComponent, childrenAnswers);
    }

}
