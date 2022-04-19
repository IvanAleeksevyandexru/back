package ru.gosuslugi.pgu.fs.common.component.validation;

import lombok.RequiredArgsConstructor;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;

import java.util.Map;

import static org.springframework.util.StringUtils.isEmpty;
import static ru.gosuslugi.pgu.components.ValidationUtil.mapEntry;

@RequiredArgsConstructor
public class NotBlankValidation implements ValidationRule {

    private final String errorMessage;

    @Override
    public Map.Entry<String, String> validate(Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        if (isEmpty(AnswerUtil.getValue(entry))) {
            return mapEntry(entry.getKey(), errorMessage);
        }
        return null;
    }
}
