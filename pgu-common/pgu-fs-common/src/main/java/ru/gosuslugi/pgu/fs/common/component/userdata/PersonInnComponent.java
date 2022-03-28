package ru.gosuslugi.pgu.fs.common.component.userdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.common.utils.StringConvertHelper;

import java.util.*;

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

    /**
     * Допустимая маска ввода XXXXXXXXXXXX или XXXX-XXXXXX-XX
     */
    private final String VALUE_REG_EXP_MASK = "\\d{12}|\\d{4}-\\d{6}-\\d{2}";
    private final List<Integer> factors1 = List.of(7, 2, 4, 10, 3, 5, 9, 4, 6, 8);
    private final List<Integer> factors2 = List.of(3, 7, 2, 4, 10, 3, 5, 9, 4, 6, 8);
    private final String DEFAULT_ERROR_MSG = "Некорректный ИНН юридического лица";

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        String value = AnswerUtil.getValue(entry);
        String errorMessage = InnValidationHelper.getErrorMessage(fieldComponent, DEFAULT_ERROR_MSG);
        InnValidationHelper.regExValidation(incorrectAnswers, entry, fieldComponent, value, VALUE_REG_EXP_MASK, errorMessage);

        if (!incorrectAnswers.isEmpty() || !hasText(value)) {
            return;
        }
        if (!personInnValidation(value)) {
            incorrectAnswers.put(entry.getKey(), errorMessage);
        }
    }


    @Override
    public ComponentType getType() {
        return ComponentType.PersonInnInput;
    }

    /**
     * Валидация контрольной суммы ИНН ЮЛ
     *
     * @param innString ИНН
     * @return false - валидация не пройдена
     */
    private boolean personInnValidation(String innString) {
        List<Integer> figures = StringConvertHelper.getFiguresListFromString(innString);
        Integer firstSum = InnValidationHelper.getCheckSum(Collections.unmodifiableList(factors1), figures);
        int figureValue = InnValidationHelper.getFigureValue(firstSum, InnValidationHelper.INN_DIVIDER_VALUE);
        figureValue = figureValue == 10 ? 0 : figureValue;
        if (figures.get(10) != figureValue) return false;
        Integer secondSum = InnValidationHelper.getCheckSum(factors2, figures);
        int secondFigureValue = InnValidationHelper.getFigureValue(secondSum, InnValidationHelper.INN_DIVIDER_VALUE);
        return figures.get(11) == secondFigureValue | figures.get(11) == 0;
    }
}
