package ru.gosuslugi.pgu.fs.common.component.validation;

import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

import java.util.Map;

public class ValidationRuleImpl implements ValidationRule {

    private final ValidationFunction validationFunction;

    public ValidationRuleImpl(ValidationFunction validationFunction) {
        this.validationFunction = validationFunction;
    }

    @Override
    public Map.Entry<String, String> validate(Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        return validationFunction.validate(entry, fieldComponent);
    }

    @FunctionalInterface
    public interface ValidationFunction {

        Map.Entry<String, String> validate(Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent);

    }
}
