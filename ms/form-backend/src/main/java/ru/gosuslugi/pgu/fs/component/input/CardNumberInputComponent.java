package ru.gosuslugi.pgu.fs.component.input;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractCycledComponent;
import ru.gosuslugi.pgu.fs.common.component.validation.RegExpValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.RequiredNotBlankValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;

import java.util.List;
import java.util.Map;

import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.CardNumberInput;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardNumberInputComponent extends AbstractCycledComponent<String> {

    @Override
    public ComponentType getType() {
        return CardNumberInput;
    }

    @Override
    public List<ValidationRule> getValidations() {
        return List.of(
                new RequiredNotBlankValidation("Поле обязательно для заполнения"),
                new RegExpValidation()
        );
    }

    @Override
    public void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        String value = AnswerUtil.getValue(entry);
        if (StringUtils.isBlank(value)) {
            incorrectAnswers.put(entry.getKey(), "Номер карты не может быть пустой строкой или строкой с пробелами");
            return;
        }
        if (!checkLuhn(value)) {
            incorrectAnswers.put(entry.getKey(), "Такой карты не существует");
        }
    }

    /**
     * Алгоритм Луна верификации правильности ввода номеров карт с учётом 19-значных карт МИР.
     *
     * @param cardNumber номер карты без пробелов
     * @return результат верификации
     * @see <a href="https://www.geeksforgeeks.org/luhn-algorithm/">Luhn algorithm</a>
     */
    private static boolean checkLuhn(String cardNumber) {
        int sum = 0;
        String digits = StringUtils.getDigits(cardNumber);
        int length = digits.length();
        boolean isEven = length % 2 == 0;
        for (int i = 0; i < length; i++) {
            int cardNum = digits.charAt(i) - '0';
            if (isEven ? i % 2 == 0 : i % 2 == 1) {
                cardNum = cardNum * 2;
                if (cardNum > 9) {
                    cardNum = cardNum - 9;
                }
            }
            sum += cardNum;
        }
        return sum % 10 == 0;
    }
}
