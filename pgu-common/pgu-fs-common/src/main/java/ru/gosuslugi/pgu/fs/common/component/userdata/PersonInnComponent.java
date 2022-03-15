package ru.gosuslugi.pgu.fs.common.component.userdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.common.utils.InnUtil;

import java.util.Map;

import static org.springframework.util.StringUtils.hasText;

/**
 * Класс реализует валидацию  ИНН физ. лица
 * Предустановлены выражения для валидации по regExp
 * Реализована валидация по контольным суммам
 * - в последующем компонент должен быть отнаследован от StringInput
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PersonInnComponent extends AbstractComponent<String> {


    private final String DEFAULT_ERROR_MSG = "Некорректный ИНН юридического лица";

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        String value = AnswerUtil.getValue(entry);
        String errorMessage = InnValidationHelper.getErrorMessage(fieldComponent, DEFAULT_ERROR_MSG);
        InnValidationHelper.regExValidation(incorrectAnswers, entry, fieldComponent, value, InnUtil.PERSON_INN_PATTERN.pattern(), errorMessage);

        if (!incorrectAnswers.isEmpty() || !hasText(value)) {
            return;
        }
        if (!InnUtil.personInnValidation(value)) {
            incorrectAnswers.put(entry.getKey(), errorMessage);
        }
    }

    @Override
    public ComponentType getType() {
        return ComponentType.PersonInnInput;
    }

}
