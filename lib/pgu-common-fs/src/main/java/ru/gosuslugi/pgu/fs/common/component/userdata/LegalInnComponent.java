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

import java.util.List;
import java.util.Map;

import static org.springframework.util.StringUtils.hasText;

/**
 * ИНН юридического лица
 * Предустановлены выражения для валидации по regExp
 * Реализована валидация по контольным суммам
 * - в последующем компонент должен быть отнаследован от StringInput
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LegalInnComponent extends AbstractComponent<String> {

    /**
     * Допустимая маска ввода XXXXXXXXXX или XXXX-XXXXX-X
     */
    private final String VALUE_REG_EXP_MASK = "\\d{10}|\\d{4}-\\d{5}-\\d";
    private final List<Integer> factors = List.of(2, 4, 10, 3, 5, 9, 4, 6, 8);
    private final String DEFAULT_ERROR_MSG = "Некорректный ИНН юридического лица";

    @Override
    public ComponentType getType() {
        return ComponentType.LegalInnInput;
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        String value = AnswerUtil.getValue(entry);
        String errorMessage = InnValidationHelper.getErrorMessage(fieldComponent, DEFAULT_ERROR_MSG);
        InnValidationHelper.regExValidation(incorrectAnswers, entry, fieldComponent, value, VALUE_REG_EXP_MASK, errorMessage);

        if (!incorrectAnswers.isEmpty() || !hasText(value)) {
            return;
        }
        if (!legalInnValidation(value)) {
            incorrectAnswers.put(entry.getKey(), errorMessage);
        }
    }

    /**
     * Валидация контрольной суммы ИНН ЮЛ
     *
     * @param innString ИНН
     * @return fasle - валидация не пройдена
     */
    private boolean legalInnValidation(String innString) {
        List<Integer> figures = StringConvertHelper.getFiguresListFromString(innString);
        Integer sum = InnValidationHelper.getCheckSum(factors, figures);
        int tenthFigure = InnValidationHelper.getFigureValue(sum, InnValidationHelper.INN_DIVIDER_VALUE);
        tenthFigure = tenthFigure == 10 ? 0 : tenthFigure;
        return tenthFigure == figures.get(9);
    }

}
