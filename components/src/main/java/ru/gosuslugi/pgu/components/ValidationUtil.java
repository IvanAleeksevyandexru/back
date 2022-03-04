package ru.gosuslugi.pgu.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.components.descriptor.types.ValidationFieldDto;
import ru.gosuslugi.pgu.components.dto.ErrorDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil.jsonNodeToString;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ERROR_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ERROR_DESC_ATTR;
import static ru.gosuslugi.pgu.components.FieldComponentUtil.VALIDATION_ARRAY_KEY;
import static ru.gosuslugi.pgu.components.RegExpUtil.*;

public class ValidationUtil {

    public static final String VALIDATION_TYPE = "type";
    public static final String MAX_LENGTH_VALIDATION_TYPE = "maxLength";
    private ValidationUtil() {
    }


    /**
     * @param name имя (ключ)
     * @param value значение
     * @param fieldComponent компонент
     * @param errorMessage сообщение об ошибке
     * @return Map.Entry<String, String> или нуль
     */
    public static Map.Entry<String, String> validateRequiredNotNull(String name, String value, FieldComponent fieldComponent, String errorMessage) {
        return validateRequired(name, value, fieldComponent, errorMessage, v -> !isNull(v));
    }

    /**
     * @param name имя (ключ)
     * @param value значение
     * @param fieldComponent компонент
     * @param errorMessage сообщение об ошибке
     * @return Map.Entry<String, String> или нуль
     */
    public static Map.Entry<String, String> validateRequiredNotBlank(String name, String value, FieldComponent fieldComponent, String errorMessage) {
        return validateRequired(name, value, fieldComponent, errorMessage, v -> (!isNull(v) && !v.isBlank()));
    }

    /**
     * @param name имя (ключ)
     * @param value значение
     * @param fieldComponent компонент
     * @return Map.Entry<String, String> или нуль
     */
    public static Map.Entry<String, String> validateRegExp(String name, String value, FieldComponent fieldComponent) {
        Map.Entry<String, String> result = null;
        List<Map<String, String>> validationRegExpList = RegExpUtil.getValidationRegExpList(fieldComponent);
        List<String> errors = RegExpUtil.validate(validationRegExpList, value);
        if (!errors.isEmpty()) {
            result = mapEntry(name, String.join(", ", errors));
        }
        return result;
    }

    /**
     * Валидация максимальной длины
     *
     * @param name имя (ключ)
     * @param value значение
     * @param fieldComponent компонент
     * @return Map.Entry<String, String> или нуль
     */
    public static Map.Entry<String, String> validateMaxLength(String name, String value, FieldComponent fieldComponent) {
        Optional<Map<String, String>> validation = FieldComponentUtil.getStringList(fieldComponent, FieldComponentUtil.VALIDATION_ARRAY_KEY, true).stream()
                .filter(
                        validationRule ->
                                MAX_LENGTH_VALIDATION_TYPE.equalsIgnoreCase(validationRule.get(VALIDATION_TYPE))
                                        && StringUtils.hasText(validationRule.get("value"))
                )
                .findFirst();

        Map.Entry<String, String> result = null;
        if (validation.isPresent()) {
            int maxLength = Integer.parseInt(validation.get().get("value"));
            String errorMsg = validation.get().get("errorMsg");
            result = validateRequired(name, value, fieldComponent,
                    errorMsg.replace("${" + MAX_LENGTH_VALIDATION_TYPE + "}", Integer.toString(maxLength)), v -> (v.length() <= maxLength));
        }

        return result;
    }

    /**
     * null cann't be members! if it is this case then null will return
     *
     * @param name имя (ключ)
     * @param value значение
     * @param fieldComponent компонент
     * @param errorMessage сообщение об ошибке
     * @return Map.Entry<String, String> или нуль
     */
    public static Map.Entry<String, String> validateMemberValueOfList(String name, String value, FieldComponent fieldComponent, String errorMessage) {
        Map.Entry<String, String> result = null;
        List<Map<String, String>> members = FieldComponentUtil.getStringList(fieldComponent, FieldComponentUtil.DICTIONARY_LIST_KEY, false);
        if (!isNull(value) && !isNull(members)) {
            boolean existed = members
                    .stream()
                    .anyMatch(
                            expectedMap ->
                                    expectedMap
                                            .values()
                                            .stream()
                                            .map(v -> isNull(v) ? null : v)
                                            .anyMatch(expectedValue -> Objects.equals(expectedValue, value))
                    );
            if (!existed) {
                result = mapEntry(name, errorMessage);
            }
        }
        return result;
    }

    private static Map.Entry<String, String> validateRequired(
            String name,
            String value,
            FieldComponent fieldComponent,
            String errorMessage,
            Predicate<String> validPredicate
    ) {
        Map.Entry<String, String> result = null;
        if (!validPredicate.test(value)) {
            if (fieldComponent.isRequired()) {
                result = mapEntry(name, errorMessage);
            }
        }
        return result;
    }

    /**
     * @param fieldComponent fieldComponent
     * @return list
     */
    public static List<Map<Object, Object>> getValidationList(
            FieldComponent fieldComponent,
            String type
    ) {
        return FieldComponentUtil.getList(fieldComponent, FieldComponentUtil.VALIDATION_ARRAY_KEY, true)
                .stream()
                .filter(validationRule -> type.equals(validationRule.get(ValidationUtil.VALIDATION_TYPE)))
                .collect(Collectors.toList());
    }

    public static Map.Entry<String, String> mapEntry(String key, String value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    /**
     * Валидация конкретного field внутри массива fields, заданного через "fieldName"
     */
    public static Map<String, ErrorDto> validateFieldsByRegExp(
            Map<String, String> incorrectAnswers,
            String value,
            List<ValidationFieldDto> fields
    ) throws JsonProcessingException {
        Map<String, ErrorDto> errorsMap = new HashMap<>();

        if (CollectionUtils.isEmpty(fields)) {
            return errorsMap;
        }

        JsonNode documentJson = JsonProcessingUtil.getObjectMapper().readTree(value);
        fields.forEach(field -> Optional.ofNullable(field)
                .map(ValidationFieldDto::getAttrs)
                .filter(map -> map.get(VALIDATION_ARRAY_KEY) instanceof List)
                .map(map -> (List<Map<String, String>>) map.get(VALIDATION_ARRAY_KEY))
                .stream()
                .flatMap(Collection::stream)
                .filter(validationRule -> REG_EXP_TYPE.equalsIgnoreCase(validationRule.get("type"))
                        && StringUtils.hasText(validationRule.get(REG_EXP_VALUE)))
                .forEach(validationRule -> {
                    JsonNode jsonObj = documentJson.findValue(field.getFieldName());
                    String stringToCheck = jsonNodeToString(jsonObj);
                    if (
                            !incorrectAnswers.containsKey(field.getFieldName())
                                    && !StringUtils.isEmpty(stringToCheck)
                                    && !stringToCheck.matches(validationRule.get(REG_EXP_VALUE))
                    ) {
                        errorsMap.put(
                                field.getFieldName(),
                                new ErrorDto(
                                        "red-line",
                                        ERROR_ATTR,
                                        validationRule.get(REG_EXP_ERROR_MESSAGE),
                                        Optional.ofNullable(validationRule.get(ERROR_DESC_ATTR))
                                                .orElse(" "),
                                        null
                                )
                        );
                    }
                }));
        return errorsMap;
    }
}
