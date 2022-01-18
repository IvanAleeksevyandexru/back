package ru.gosuslugi.pgu.components;

import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public class RegExpUtil {


    public final static String REG_EXP_TYPE = "RegExp";
    public final static String REG_EXP_VALUE = "value";
    public final static String REG_EXP_ERROR_MESSAGE = "errorMsg";

    private RegExpUtil() {
    }


    /**
     * @param fieldComponent fieldComponent
     * @return list
     */
    public static List<Map<String, String>> getValidationRegExpList(FieldComponent fieldComponent) {
        return FieldComponentUtil.getStringList(fieldComponent, FieldComponentUtil.VALIDATION_ARRAY_KEY, true)
            .stream()
            .filter(
                validationRule ->
                    RegExpUtil.REG_EXP_TYPE.equalsIgnoreCase(validationRule.get(ValidationUtil.VALIDATION_TYPE))
                    && StringUtils.hasText(validationRule.get(RegExpUtil.REG_EXP_VALUE))
            )
            .collect(Collectors.toList());
    }

    /**
     * @param validationRegExpList validationRegExpList
     * @param value value
     * @return list
     */
    public static List<String> validate(List<Map<String, String>> validationRegExpList, String value) {
        List<String> result;
        if (isNull(value)) {
            result = Collections.emptyList();
        } else {
            result = validationRegExpList
                .stream()
                .map(
                    validationRule ->
                        value.matches(validationRule.get(REG_EXP_VALUE))
                        ? null
                        : validationRule.get(REG_EXP_ERROR_MESSAGE)
                )
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        }
        return result;
    }
}
