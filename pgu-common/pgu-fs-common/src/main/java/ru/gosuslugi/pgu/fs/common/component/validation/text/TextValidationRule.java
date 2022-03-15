package ru.gosuslugi.pgu.fs.common.component.validation.text;

import org.apache.commons.lang.StringUtils;
import ru.gosuslugi.pgu.components.ValidationUtil;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;

import java.util.Map;

public abstract class TextValidationRule implements ValidationRule {

    public abstract String getValidationType();

    public abstract String getDefaultErrorMessage();

    protected abstract boolean isValid(Map<Object, Object> params, String value, String key, ScenarioDto scenario);

    @Override
    public Map.Entry<String, String> validate(Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent, ScenarioDto scenarioDto) {
        final var value = AnswerUtil.getValueOrNull(entry);
        if (StringUtils.isBlank(value)) {
            return null;
        }

        final var validations = ValidationUtil.getValidationList(fieldComponent, getValidationType());
        for (final var validation: validations) {
            final var isValid = isValid(validation, value, entry.getKey(), scenarioDto);
            if (!isValid) {
                return Map.entry(entry.getKey(), getErrorMessage(validation));
            }
        }
        return null;
    }

    @Override
    public Map.Entry<String, String> validate(Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        return validate(entry, fieldComponent, null);
    }

    private String getErrorMessage(Map<Object, Object> params) {
        final var msg = params.get("errorMsg");
        if (!(msg instanceof String)) {
            return getDefaultErrorMessage();
        }
        return (String) msg;
    }
}
