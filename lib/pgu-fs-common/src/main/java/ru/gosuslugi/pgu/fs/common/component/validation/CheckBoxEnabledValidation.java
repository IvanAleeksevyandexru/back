package ru.gosuslugi.pgu.fs.common.component.validation;

import lombok.RequiredArgsConstructor;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

import java.util.Map;

import static ru.gosuslugi.pgu.fs.common.utils.ConditionalDisabledComponentUtil.isCheckBoxValidationDisabled;

@RequiredArgsConstructor
public class CheckBoxEnabledValidation implements ValidationRule {

    private final ValidationRule delegateRule;

    @Override
    public Map.Entry<String, String> validate(Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent, ScenarioDto scenarioDto) {
        if (!isCheckBoxValidationDisabled(scenarioDto, fieldComponent)) {
            return delegateRule.validate(entry, fieldComponent, scenarioDto);
        }
        return null;
    }
}
