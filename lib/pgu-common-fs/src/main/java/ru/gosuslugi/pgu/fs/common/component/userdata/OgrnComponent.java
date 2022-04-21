package ru.gosuslugi.pgu.fs.common.component.userdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.validation.EmptyOr;
import ru.gosuslugi.pgu.fs.common.component.validation.RequiredNotBlankValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRuleImpl;
import ru.gosuslugi.pgu.fs.common.utils.StringConvertHelper;

import java.util.List;

import static ru.gosuslugi.pgu.components.ValidationUtil.mapEntry;

/**
 * Класс содержит вспомогательные методы компонента ОГРН
 * - в последующем компонент должен быть отнаследован от StringInput
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OgrnComponent extends AbstractComponent<String> {

    private final String DEFAULT_ERROR_MESSAGE = "Некорректный ОГРН";

    @Override
    public ComponentType getType() {
        return ComponentType.OgrnInput;
    }

    @Override
    public List<ValidationRule> getValidations() {
        return List.of(
                new RequiredNotBlankValidation(getErrorText()),
                new EmptyOr(new ValidationRuleImpl((entry, fieldComponent) -> {
                    if (!checkOgrn(AnswerUtil.getValue(entry))) {
                        return mapEntry(entry.getKey(), getErrorText());
                    }
                    return null;
                }))
        );
    }

    /**
     * Получить значение делителя, используемое в алгоритме проверки
     *
     * @return
     */
    protected int getCheckOgrnDivider() {
        return 11;
    }

    /**
     * Возможные форматы ввода ОГРН XXXXXXXXXXXXX или X-XX-XX-XXXXXXX-X
     *
     * @return
     */
    protected String getValueRegExp() {
        return "\\d{13}|\\d-\\d{2}-\\d{2}-\\d{7}-\\d";
    }

    /**
     * Получить текст ошибки по умолчанию
     *
     * @return
     */
    protected String getErrorText() {
        return DEFAULT_ERROR_MESSAGE;
    }

    /**
     * Проверка контрольной суммы ОГРН
     *
     * @param value ОГРН
     * @return false - валидация не пройдена
     */
    private boolean checkOgrn(String value) {
        if (!value.matches(getValueRegExp())) {
            return false;
        }
        String digitsValue = StringConvertHelper.getDigitString(value);
        String checkValueString = digitsValue.substring(0, digitsValue.length() - 1);
        long checkValue = Long.parseLong(checkValueString);
        int lastFigure = Integer.parseInt(digitsValue.substring(digitsValue.length() - 1));
        long resultFigure = checkValue % getCheckOgrnDivider();
        // сравнивается только младший разряд
        resultFigure = resultFigure % 10;
        return resultFigure == lastFigure;
    }
}
