package ru.gosuslugi.pgu.fs.component.gender;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.service.DictionaryFilterService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static ru.gosuslugi.pgu.components.ComponentAttributes.DICTIONARY_FILTER_NAME_ATTR;
import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.Dictionary;
import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.GDictionary;

@Component
@RequiredArgsConstructor
public class DictionaryGenderComponent extends AbstractDictionaryGenderComponent<String> {

    private final DictionaryFilterService dictionaryFilterService;

    @Override
    public ComponentType getType() {
        return GDictionary;
    }

    @Override
    protected ComponentType getTargetComponentType() {
        return Dictionary;
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
        if (StringUtils.hasText(AnswerUtil.getValue(entry))) {
            dictionaryFilterService.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent, () -> getInitialValue(fieldComponent, scenarioDto));
        }
    }
}
