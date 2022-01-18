package ru.gosuslugi.pgu.fs.common.component.validation;

import lombok.RequiredArgsConstructor;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;

import java.util.Map;

import static org.springframework.util.StringUtils.hasText;

/**
 * Делегирует вызов вложенному валидатору только если значение не пустое
 */
@RequiredArgsConstructor
public class EmptyOr implements ValidationRule {

    private final ValidationRule delegateRule;

    @Override
    public Map.Entry<String, String> validate(Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent, ScenarioDto scenarioDto) {
        if (hasText(AnswerUtil.getValue(entry))) {
            return delegateRule.validate(entry, fieldComponent, scenarioDto);
        }
        return null;
    }
}
