package ru.gosuslugi.pgu.fs.common.component.userdata;

import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;

/**
 * Класс содержит методы, используемые в алгоритмах проверки контрольной сумма ИНН
 */
public abstract class InnValidationHelper {

    public static final int INN_DIVIDER_VALUE = 11;

    private static final String VALIDATION_ARRAY_KEY = "validation";
    private static final String REG_EXP_ERROR_MESSAGE = "errorMsg";

    /**
     * Формирование контрольной суммы с перемножением элементов на сходных позициях
     *
     * @param factors
     * @param figures
     * @return
     */
    public static Integer getCheckSum(List<Integer> factors, List<Integer> figures) {
        List<Integer> resultList = new ArrayList<>();
        for (int pos = 0; pos < factors.size(); pos++) {
            resultList.add(factors.get(pos) * figures.get(pos));
        }
        Integer sum = resultList.stream()
                .collect(Collectors.summingInt(Integer::intValue));
        return sum;
    }

    /**
     * @param sumValue     сумма первых цифр ИНН с учетом весовых компонентов
     * @param dividerValue значения делителя (задано алгоритмом проверки)
     * @return контольное значение
     */
    public static int getFigureValue(int sumValue, int dividerValue) {
        int modValue = sumValue / dividerValue;
        int multValue = modValue * dividerValue;
        return sumValue - multValue;
    }

    /**
     * Получить сообщение об ошибке
     *
     * @param fieldComponent описание поля
     * @param defaultMessage сообщение по умолчанию
     * @return текст сообщения об ошибке
     */
    public static String getErrorMessage(FieldComponent fieldComponent, String defaultMessage) {
        Optional<String> errorMsg = Optional.ofNullable((List<Map<String, String>>) fieldComponent.getAttrs().get(VALIDATION_ARRAY_KEY))
                .stream()
                .flatMap(Collection::stream).findFirst()
                .stream()
                .findFirst().map(validationRule -> validationRule.get(REG_EXP_ERROR_MESSAGE));
        return errorMsg.orElse(defaultMessage);
    }

    /**
     * Базовая валидация значения на заполненность и соответствие regExp
     *
     * @param entry          ответы пользователя
     * @param fieldComponent описание компонента
     * @param value          значение компонента
     * @param regExString    строка регулярного выражения
     * @param errorMessage   текст ошибки
     * @return список ошибок
     */
    public static Map<String, String> regExValidation(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry,
                                                      FieldComponent fieldComponent, String value, String regExString, String errorMessage) {
        if (!hasText(value) && fieldComponent.isRequired()) {
            incorrectAnswers.put(entry.getKey(), errorMessage);
        }
        if (hasText(value) && !value.matches(regExString)) {
                incorrectAnswers.put(entry.getKey(), errorMessage);
        }
        return incorrectAnswers;
    }
}
