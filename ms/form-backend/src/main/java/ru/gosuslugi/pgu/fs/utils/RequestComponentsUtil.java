package ru.gosuslugi.pgu.fs.utils;

import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static ru.gosuslugi.pgu.components.ComponentAttributes.METHOD_ATTR;
import static ru.gosuslugi.pgu.fs.common.service.ReferenceService.placeholderPattern;

/**
 * Утильный класс для работы с группой компонент для вызвов внешних API
 * содержит методы:
 * - базовой проверки наличия обязательных аттрибутов
 * - получения аргументов из значений черновика и подстановку
 *
 */
public class RequestComponentsUtil {

    public static Map<String, String> getAttrAsMap(String key, Map<String, Object> map) {
        return (Map<String, String>) map.getOrDefault(key, new HashMap<>());
    }

    public static Map<String, String> resolveMapValueArguments(Map<String, String> map, Map<String, String> arguments) {
        return map.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> resolveArguments(e.getValue(), arguments)));
    }

    public static String resolveArguments(String value, Map<String, String> arguments) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }

        final Matcher m = placeholderPattern.matcher(value);
        while(m.find()) {
            value = value.replace(m.group(), arguments.getOrDefault(m.group(1), ""));
        }

        return value;
    }

    public static void validateComponentAttrs(List<String> attrsList, Map<String, Object> attrs) {
        if (!attrsList.stream().allMatch(attrs::containsKey)) {
            throw new FormBaseException("Заданы не все обязательные атрибуты компонента");
        }

        Arrays.stream(HttpMethod.values())
                .filter(v -> v.name().equalsIgnoreCase(attrs.get(METHOD_ATTR).toString()))
                .findFirst()
                .orElseThrow(() -> new FormBaseException("Неверное значение атрибута method: " + attrs.get(METHOD_ATTR)));
    }
}
