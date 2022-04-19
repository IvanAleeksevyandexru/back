package ru.gosuslugi.pgu.fs.common.component.validation;

import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.components.ValidationUtil;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;

import java.util.AbstractMap;
import java.util.Map;

public class RegExpValidation implements ValidationRule {

    private String regex;
    private String errorMessage;

    /**
     * При дефолтном конструкторе regexp правила для валидации берутся из описания компонента
     */
    public RegExpValidation() {
    }

    public RegExpValidation(String regex, String errorMessage) {
        this.regex = regex;
        this.errorMessage = errorMessage;
    }

    @Override
    public Map.Entry<String, String> validate(Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        if (regex == null) {
            return ValidationUtil.validateRegExp(entry.getKey(), AnswerUtil.getValue(entry), fieldComponent);
        }
        if (!AnswerUtil.getValue(entry).matches(regex)) {
            return new AbstractMap.SimpleEntry<>(entry.getKey(), errorMessage);
        }
        return null;
    }
}
