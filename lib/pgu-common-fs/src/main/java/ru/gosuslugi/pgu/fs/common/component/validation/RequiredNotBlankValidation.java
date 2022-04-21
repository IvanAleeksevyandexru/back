package ru.gosuslugi.pgu.fs.common.component.validation;

import lombok.RequiredArgsConstructor;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.components.ValidationUtil;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;

import java.util.Map;

@RequiredArgsConstructor
public class RequiredNotBlankValidation implements ValidationRule {

    private final String errorMessage;

    @Override
    public Map.Entry<String, String> validate(Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        return ValidationUtil.validateRequiredNotBlank(entry.getKey(), AnswerUtil.getValue(entry), fieldComponent, errorMessage);
    }
}
