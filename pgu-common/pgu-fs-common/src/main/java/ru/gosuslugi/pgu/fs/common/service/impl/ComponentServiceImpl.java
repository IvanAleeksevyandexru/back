package ru.gosuslugi.pgu.fs.common.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.core.exception.ValidationException;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.components.BasicComponentUtil;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.fs.common.component.AbstractCycledComponent;
import ru.gosuslugi.pgu.fs.common.component.BaseComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentRegistry;
import ru.gosuslugi.pgu.fs.common.service.ComponentReferenceService;
import ru.gosuslugi.pgu.fs.common.service.ComponentService;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;


@Service
@Primary
@RequiredArgsConstructor
public class ComponentServiceImpl implements ComponentService {

    private static final String IMPLEMENTER_REGION = "implementerRegion";

    @Lazy
    @Autowired
    private ComponentRegistry componentRegistry;

    @Autowired
    private ComponentReferenceService componentReferenceService;

    @Override
    public Optional<FieldComponent> getFieldComponent(String serviceId, Map.Entry<String, ApplicantAnswer> answerEntry, ServiceDescriptor serviceDescriptor) {
        var componentId = answerEntry.getKey();
        var componentOptional =  serviceDescriptor.getApplicationFields()
                .stream()
                .filter(field -> field.getId().equals(componentId))
                .map(FieldComponent::getCopy)
                .findAny();
        /** Аналитику по компонентам следует отправлять в случае если analyticsTag пустой,
         *  или в analyticsTag присутствует id компонента
         *  */
        var analyticsTags = serviceDescriptor.getAnalyticsTags();
        if (componentOptional.isPresent()) {
            if (analyticsTags.isEmpty() ||
                    analyticsTags
                            .stream()
                            .filter(tag -> StringUtils.hasText(tag.getComponentId()))
                            .filter(tag-> IMPLEMENTER_REGION.equals(tag.getName()))
                            .anyMatch(v -> v.getComponentId().equals(componentId))
            ) {
                componentOptional.get().setSendAnalytics(true);
            }
        }
        return componentOptional;
    }

    @Override
    public List<FieldComponent> getScreenFields(ScreenDescriptor screenDescriptor, ScenarioDto scenarioDto, ServiceDescriptor currentDescriptor) {
        var result  = this.getMainComponents(screenDescriptor,scenarioDto,currentDescriptor);
        result.addAll(getInfoComponents(screenDescriptor,scenarioDto,currentDescriptor));
        result.addAll(getLogicAfterValidationComponents(screenDescriptor,scenarioDto,currentDescriptor));
        return result;
    }

    @Override
    public List<FieldComponent> getLogicFields(ScreenDescriptor screenDescriptor, ScenarioDto scenarioDto, ServiceDescriptor currentDescriptor) {
        List<String> ids = screenDescriptor.getLogicComponentIds();
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }

        List<FieldComponent> logicComponents = new LinkedList<>();
        ids.forEach(id -> {
            List<FieldComponent> fieldComponents = currentDescriptor.getApplicationFields()
                    .stream()
                    .filter(field -> field.getId().equals(id))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(fieldComponents)) {
                throw new  ValidationException("Cannot find logic component by id " + id);
            }
            fieldComponents.forEach(e -> logicComponents.add(this.processField(e, scenarioDto, currentDescriptor)));
        });
        return logicComponents;
    }

    /**
     * Method for loading and pre-fill
     */
    @Override
    public FieldComponent processField(FieldComponent fieldComponent, ScenarioDto scenarioDto, ServiceDescriptor currentDescriptor) {
        fieldComponent = FieldComponent.getCopy(fieldComponent);
        componentReferenceService.processComponentRefs(fieldComponent, scenarioDto);
        BaseComponent<?> component = componentRegistry.getComponent(fieldComponent.getType());
        if (nonNull(component)) {
            component.process(fieldComponent, scenarioDto, currentDescriptor);
        }
        return fieldComponent;
    }

    @Override
    public void fillCycledField(FieldComponent fieldComponent, ScenarioDto scenarioDto) {
        if (!fieldComponent.getAttrs().containsKey(BasicComponentUtil.PRESET_ATTR_NAME)) {
            return;
        }

        CycledApplicantAnswer answer = scenarioDto.getCycledApplicantAnswers().getCurrentAnswer();
        BaseComponent<?> component = componentRegistry.getComponent(fieldComponent.getType());
        if (component != null && component.isCycled() && nonNull(answer)) {
            ((AbstractCycledComponent<?>) component).processCycledComponent(fieldComponent, answer.getCurrentAnswerItem());
        }
    }

    @Override
    public void preloadComponents(String screenId, ScenarioDto scenarioDto, ServiceDescriptor currentDescriptor) {
        ScreenDescriptor screenDescriptor = currentDescriptor.getScreens().stream().filter(screen -> screenId.equals(screen.getId())).findFirst().orElse(null);
        if(nonNull(screenDescriptor) && nonNull(screenDescriptor.getPreloadComponents())){
            screenDescriptor.getPreloadComponents().forEach(id ->
                    currentDescriptor.getApplicationFields()
                            .stream()
                            .filter(field -> field.getId().equals(id))
                            .forEach(e -> this.preloadComponent(e, scenarioDto, currentDescriptor)));
        }
    }

    @Override
    public void executeComponentAfterOrderCreated(ScreenDescriptor screenDescriptor, ScenarioDto scenarioDto, ServiceDescriptor currentDescriptor) {
        List<String> ids = screenDescriptor.getAfterOrderCreatedComponentIds();
        if (CollectionUtils.isEmpty(ids)) return;

        ids.forEach(id -> {
            List<FieldComponent> fieldComponents = currentDescriptor.getApplicationFields()
                    .stream()
                    .filter(field -> field.getId().equals(id))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(fieldComponents)) {
                throw new  ValidationException("Cannot find executable component by id " + id);
            }
            fieldComponents.forEach(e -> this.processField(e, scenarioDto, currentDescriptor));
        });
    }

    private void preloadComponent(FieldComponent fieldComponent, ScenarioDto scenarioDto, ServiceDescriptor currentDescriptor){
        fieldComponent = FieldComponent.getCopy(fieldComponent);
        BaseComponent<?> component = componentRegistry.getComponent(fieldComponent.getType());
        if (nonNull(component)) {
            component.process(fieldComponent, scenarioDto, currentDescriptor);
            component.preloadComponent(fieldComponent, scenarioDto);
        }
    }

    private List<FieldComponent> getLogicAfterValidationComponents(ScreenDescriptor screenDescriptor, ScenarioDto scenarioDto, ServiceDescriptor currentDescriptor){

        List<String> ids = screenDescriptor.getLogicAfterValidationComponentIds();
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<FieldComponent> components = new LinkedList<>();
        ids.forEach(id -> {
            List<FieldComponent> fieldComponents = currentDescriptor.getApplicationFields()
                    .stream()
                    .filter(field -> field.getId().equals(id))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(fieldComponents)) {
                throw new  ValidationException("Cannot find LogicAfterValidation fieldComponent by id " + id);
            }
            fieldComponents.forEach(e -> components.add(this.processField(e, scenarioDto, currentDescriptor)));
        });
        return components;
    }

    private List<FieldComponent> getInfoComponents(ScreenDescriptor screenDescriptor, ScenarioDto scenarioDto, ServiceDescriptor currentDescriptor){
        List<FieldComponent> components = new LinkedList<>();
        screenDescriptor.getInfoComponents().forEach(id -> {
            List<FieldComponent> fieldComponents = currentDescriptor.getApplicationFields()
                    .stream()
                    .filter(field -> field.getId().equals(id))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(fieldComponents)) {
                throw new  ValidationException("Cannot find Info fieldComponent by id " + id);
            }
            fieldComponents.forEach(e -> components.add(this.processField(e, scenarioDto, currentDescriptor)));
        });
        return components;
    }

    private List<FieldComponent> getMainComponents(ScreenDescriptor screenDescriptor, ScenarioDto scenarioDto, ServiceDescriptor currentDescriptor){
        List<FieldComponent> components = new LinkedList<>();
        screenDescriptor.getComponentIds().forEach(id -> {
            List<FieldComponent> fieldComponents = currentDescriptor.getApplicationFields()
                    .stream()
                    .filter(field -> field.getId().equals(id))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(fieldComponents)) {
                throw new  ValidationException("Cannot find Main FieldComponent by id " + id);
            }
            fieldComponents.forEach(e -> components.add(this.processField(e, scenarioDto, currentDescriptor)));
        });
        return components;
    }
}
