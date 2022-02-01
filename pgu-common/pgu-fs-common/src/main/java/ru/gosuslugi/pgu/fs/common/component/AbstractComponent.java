package ru.gosuslugi.pgu.fs.common.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import ru.gosuslugi.pgu.components.descriptor.attr_factory.ClarificationAttrsFactory;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.LinkedValue;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.fs.common.service.ComponentReferenceService;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.service.LinkedValuesService;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;

import java.util.*;

public abstract class AbstractComponent<InitialValueModel> implements BaseComponent<InitialValueModel> {

    @Autowired
    protected ComponentReferenceService componentReferenceService;

    @Autowired
    protected JsonProcessingService jsonProcessingService;

    @Autowired
    protected LinkedValuesService linkedValuesService;

    @Autowired
    protected ObjectMapper objectMapper;

    @Override
    public void process(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        linkedValuesService.fillLinkedValues(component, scenarioDto);
        clarificationLinkedValues(component, scenarioDto);
        componentReferenceService.processComponentRefs(component, scenarioDto);
        preProcess(component, scenarioDto, serviceDescriptor);
        ComponentResponse<InitialValueModel> initialValue = getInitialValue(component, scenarioDto, serviceDescriptor);
        preValidate(initialValue, component, scenarioDto);
        component.setValue(jsonProcessingService.componentDtoToString(initialValue));
    }

    private void clarificationLinkedValues(FieldComponent component, ScenarioDto scenarioDto) {
        val attrs = (Map<String, Object>) component.getAttrs();
        if (attrs == null) return;
        val clarifications = (Map<String, Map<String, Object>>)attrs.get("clarifications");
        if (clarifications == null) return;

        clarifications.forEach((clKey, clValue) -> {
            val linkedValuesList = (List<Object>) clValue.get("linkedValues");
            if (linkedValuesList != null) {
                linkedValuesList.stream()
                        .map(lvObject -> objectMapper.convertValue(lvObject, LinkedValue.class))
                        .forEach(lv -> {
                            clValue.putIfAbsent("refs", new LinkedHashMap<>());
                            val value = linkedValuesService.getValue(lv, scenarioDto, new ClarificationAttrsFactory());
                            ((Map<String, String>) clValue.get("refs")).put(lv.getArgument(), value);
                        });
            }
        });
    }

    @Override
    public void preProcess(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor serviceDescriptor) {
        preProcess(component, scenarioDto);
    }

    protected void preProcess(FieldComponent component, ScenarioDto scenarioDto) {
        preProcess(component);
    }

    protected void preProcess(FieldComponent component) {}

    protected void preValidate(ComponentResponse<InitialValueModel> initialValue, FieldComponent component, ScenarioDto scenarioDto) {}

    protected void postProcess(Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        postProcess(fieldComponent, scenarioDto, AnswerUtil.getValue(entry));
    }

    protected void postProcess(FieldComponent component, ScenarioDto scenarioDto, String value) {}

    protected void validateAfterSubmit(Map<String,String> incorrectAnswers,
                                       Map.Entry<String, ApplicantAnswer> entry,
                                       ScenarioDto scenarioDto,
                                       FieldComponent fieldComponent) {
        validateAfterSubmit(incorrectAnswers, entry, fieldComponent);
    }

    protected void validateAfterSubmit(Map<String,String> incorrectAnswers,
                                       Map.Entry<String, ApplicantAnswer> entry,
                                       FieldComponent fieldComponent) {
        validateAfterSubmit(incorrectAnswers, entry.getKey(), AnswerUtil.getValue(entry));
    }

    protected void validateAfterSubmit(Map<String,String> incorrectAnswers, String key, String value) {

    }

    public Map<String, String> validate(Map.Entry<String, ApplicantAnswer> entry,
                                        ScenarioDto scenarioDto,
                                        FieldComponent fieldComponent) {
        Map<String,String> incorrectAnswers = new HashMap<>();

        if (fieldComponent.getSkipValidation()) return Collections.emptyMap();

        linkedValuesService.fillLinkedValues(fieldComponent, scenarioDto);
        componentReferenceService.processComponentRefs(fieldComponent, scenarioDto);



        getValidations().forEach(validationRule -> {
            if (!incorrectAnswers.containsKey(entry.getKey())) {
                Map.Entry<String, String> answer = validationRule.validate(entry, fieldComponent, scenarioDto);
                if (answer != null) {
                    incorrectAnswers.put(answer.getKey(), answer.getValue());
                }
            }
        });

        if (incorrectAnswers.isEmpty()) {
            validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent);
        }
        if (incorrectAnswers.isEmpty()) {
            postProcess(entry, scenarioDto, fieldComponent);
        }

        return incorrectAnswers;
    }

    public List<List<Map<String, String>>> validateItemsUniqueness(Map.Entry<String, ApplicantAnswer> entry,
                                                                   ScenarioDto scenarioDto,
                                                                   FieldComponent fieldComponent) {
        return Collections.emptyList();
    }

}
