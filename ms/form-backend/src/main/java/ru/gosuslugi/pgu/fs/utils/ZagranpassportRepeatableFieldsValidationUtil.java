package ru.gosuslugi.pgu.fs.utils;

import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.components.RegExpUtil;
import ru.gosuslugi.pgu.components.ValidationUtil;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;

public class ZagranpassportRepeatableFieldsValidationUtil {

    public static final String CHILD_NAME_CHANGE__ALL_FIELDS_EQUAL = "zagranpassport-childNameChange-allFieldsEqual";
    private final static String KEY_VALUE = "value";

    public static final String BEFORE_SURNAME = "rf51";
    public static final String[] CURRENT_SURNAME = {"ai18_3", "ai19_3"};
    public static final String BEFORE_NAME = "rf52";
    public static final String[] CURRENT_NAME = {"ai18_4", "ai19_4"};
    public static final String BEFORE_MIDDLENAME = "rf53";
    public static final String[] CURRENT_MIDDLENAME = {"ai18_5", "ai19_5"};

    private ZagranpassportRepeatableFieldsValidationUtil() {
    }


    /**
     * @param name имя (ключ)
     * @param childrenAnswers children answers
     * @param fieldComponent компонент
     * @return Map.Entry<String, String> или нуль
     */
    public static Map.Entry<String, String> childNameChangeAllFieldsEqual(String name, List<Map<String, String>> childrenAnswers, FieldComponent fieldComponent) {
        Map.Entry<String, String> result = null;
        if (!isNull(childrenAnswers) && !childrenAnswers.isEmpty()) {
            Map<String, String> childrenAnswerMap = childrenAnswers.get(0);
            List<String> errors = ValidationUtil.getValidationList(fieldComponent, CHILD_NAME_CHANGE__ALL_FIELDS_EQUAL)
                    .stream()
                    .map(
                            rule -> (
                                    Objects.equals(getBefore(childrenAnswerMap, BEFORE_SURNAME), getCurrent(rule, CURRENT_SURNAME))
                                            && Objects.equals(getBefore(childrenAnswerMap, BEFORE_NAME), getCurrent(rule, CURRENT_NAME))
                                            && Objects.equals(getBefore(childrenAnswerMap, BEFORE_MIDDLENAME), getCurrent(rule, CURRENT_MIDDLENAME))
                            )
                                    ? Optional.ofNullable(rule.get(RegExpUtil.REG_EXP_ERROR_MESSAGE)).map(error -> error instanceof String ? (String) error : error.toString()).orElse(null)
                                    : null
                    )
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
            if (!errors.isEmpty()) {
                result = new AbstractMap.SimpleEntry<>(name, String.join(", ", errors));
            }
        }
        return result;
    }

    private static String getCurrent(Map<Object, Object> rule, String[] keys) {
        return Optional.ofNullable(rule.get(KEY_VALUE))
                .filter(v -> v instanceof Map)
                .map(v -> (Map) v)
                .map(map -> Stream.of(keys).map(key -> map.get(key)).filter(v -> !isNull(v)).findFirst().orElse(null))
                .filter(v -> v instanceof ApplicantAnswer)
                .map(v -> (ApplicantAnswer) v)
                .map(ApplicantAnswer::getValue)
                .filter(StringUtils::hasText)
                .orElse(null);
    }

    private static String getBefore(Map<String, String> childrenAnswerMap, String key) {
        return Optional.ofNullable(childrenAnswerMap.get(key))
                .filter(StringUtils::hasText)
                .orElse(null);
    }
}
