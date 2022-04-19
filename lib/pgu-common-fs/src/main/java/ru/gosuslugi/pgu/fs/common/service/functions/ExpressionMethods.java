package ru.gosuslugi.pgu.fs.common.service.functions;

import com.fasterxml.jackson.core.type.TypeReference;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ExpressionMethods {

    private static final Optional<String> OPTIONAL_NEXT = Optional.empty();
    private static final Function<Object, Map<String, Object>> OBJECT_TO_MAP_FUNCTION = el -> (Map<String, Object>) el;

    public String asString(Object value) {
        return String.valueOf(value);
    }

    public String asJson(Object value) {
        return JsonProcessingUtil.toJson(value);
    }

    public Map<String, Object> asMap(String value) {
        return JsonProcessingUtil.fromJson(value, new TypeReference<>() {});
    }

    public List<Object> asList(String value) {
        return JsonProcessingUtil.fromJson(value, new TypeReference<>() {});
    }

    public LocalDate asDate(String value) {
        return LocalDate.parse(value);
    }

    public LocalDate asDate(String value, String formatPattern) {
        return LocalDate.parse(value, DateTimeFormatter.ofPattern(formatPattern));
    }

    public LocalDateTime asDateTime(String value) {
        return LocalDateTime.parse(value);
    }

    public LocalDateTime asDateTime(String value, String formatPattern) {
        return LocalDateTime.parse(value, DateTimeFormatter.ofPattern(formatPattern));
    }

    public Integer asInt(String value) {
        return Integer.parseInt(value);
    }

    public Boolean allMatch(List<Object> objects, String node, Object flag) {
        return objects.stream()
                .filter(Map.class::isInstance)
                .map(OBJECT_TO_MAP_FUNCTION)
                .allMatch(el -> el.containsKey(node) && flag.equals(el.get(node)));
    }

    public Boolean noneMatch(List<Object> objects, String node, Object flag) {
        return objects.stream()
                .filter(Map.class::isInstance)
                .map(OBJECT_TO_MAP_FUNCTION)
                .noneMatch(el -> el.containsKey(node) && flag.equals(el.get(node)));
    }

    public Boolean anyMatch(List<Object> objects, String node, Object flag) {
        return objects.stream()
                .filter(Map.class::isInstance)
                .map(OBJECT_TO_MAP_FUNCTION)
                .anyMatch(el -> el.containsKey(node) && flag.equals(el.get(node)));
    }

    public Object getIf(boolean condition, Object arg) {
        return condition ? arg : OPTIONAL_NEXT;
    }
}
