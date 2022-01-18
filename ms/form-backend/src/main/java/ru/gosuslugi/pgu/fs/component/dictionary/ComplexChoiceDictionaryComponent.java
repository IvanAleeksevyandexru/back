package ru.gosuslugi.pgu.fs.component.dictionary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.service.DictionaryFilterService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ComplexChoiceDictionaryComponent extends AbstractComponent<String> {

    private final DictionaryFilterService dictionaryFilterService;

    @Override
    public ComponentType getType() {
        return ComponentType.ComplexChoiceDictionary;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        Map<String, Object> presetValues = new HashMap<>(dictionaryFilterService.getInitialValue(component, scenarioDto));
        return presetValues.isEmpty() ? ComponentResponse.empty() : ComponentResponse.of(jsonProcessingService.toJson(presetValues));
    }

    @Override
    public void preloadComponent(FieldComponent component, ScenarioDto scenarioDto) {
        dictionaryFilterService.preloadComponent(component, scenarioDto, () -> getInitialValue(component, scenarioDto));
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        boolean validationSwitchOn = Boolean.parseBoolean((String) fieldComponent.getAttrs().getOrDefault("validationSwitchOn", "true"));
        if (!validationSwitchOn) {
            return;
        }
        dictionaryFilterService.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent, () -> getInitialValue(fieldComponent, scenarioDto));
    }
}
