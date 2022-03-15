package ru.gosuslugi.pgu.fs.common.component.validation;

import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CompositeValidationRule implements ValidationRule {

    private final List<ValidationRule> delegates;

    public CompositeValidationRule(ValidationRule... delegates) {
        this.delegates = List.of(delegates);
    }

    @Override
    public Map.Entry<String, String> validate(Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent, ScenarioDto scenarioDto) {
        return delegates.stream().map(d -> d.validate(entry, fieldComponent, scenarioDto)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    @Override
    public Map.Entry<String, String> validate(Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        return delegates.stream().map(d -> d.validate(entry, fieldComponent)).filter(Objects::nonNull).findFirst().orElse(null);
    }
}
