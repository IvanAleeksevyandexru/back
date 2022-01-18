package ru.gosuslugi.pgu.fs.component.dictionary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.component.validation.RequiredNotBlankValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.service.DictionaryFilterService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DictionaryComponent extends AbstractComponent<String> {

    private final DictionaryFilterService dictionaryFilterService;

    @Override
    public ComponentType getType() {
        return ComponentType.Dictionary;
    }

    @Override
    public List<ValidationRule> getValidations() {
        return List.of(
                new RequiredNotBlankValidation("Поле обязательно для заполнения")
        );
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
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers,
                                       Map.Entry<String, ApplicantAnswer> entry,
                                       ScenarioDto scenarioDto,
                                       FieldComponent fieldComponent) {
        if (StringUtils.hasText(AnswerUtil.getValue(entry))) {
            dictionaryFilterService.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent, () -> getInitialValue(fieldComponent, scenarioDto));
        }
    }
}
