package ru.gosuslugi.pgu.components;

import org.springframework.util.CollectionUtils;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponentError;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ErrorType;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DEFAULT_HINT;

public class FieldComponentUtil {

    public static final String FIELDS_KEY = "fields";
    public static final String FIELD_NAME_KEY = "fieldName";
    public static final String VALIDATION_ARRAY_KEY = "validation";
    public static final String DICTIONARY_LIST_KEY = "dictionaryList";
    public static final String COMPONENTS_KEY = "components";
    public static final String UNIQUE_BY_KEY = "uniqueBy";
    public static final String IS_CYCLED_KEY = "isCycled";
    public static final String REF_KEY = "ref";
    public static final String ACTIONS_ATTR_KEY = "actions";
    public static final String ANSWERS_ATTR_KEY = "answers";
    public static final String LABEL_ATTR = "label";

    private static final String HINT_TYPE = "type";
    private static final String HINT_TITLE = "title";
    private static final String HINT_VALUE = "value";

    private static final String ATTRS_KEY = "attrs";

    private FieldComponentUtil() {
    }

    /**
     * @param fieldComponent field component
     * @return List<FieldComponent>
     */
    public static List<FieldComponent> getChildrenList(FieldComponent fieldComponent) {
        return Optional.ofNullable(fieldComponent)
            .map(FieldComponent::getAttrs)
            .filter(attr -> attr.get(COMPONENTS_KEY) instanceof List)
            .map(attr -> (List<Object>) attr.get(COMPONENTS_KEY))
            .map(list -> list.stream()
                    .filter(v -> v instanceof Map)
                    .map(v -> JsonProcessingUtil.fromJson(JsonProcessingUtil.toJson(v), FieldComponent.class))
                    .collect(Collectors.toList()))
            .orElse((List<FieldComponent>) Collections.EMPTY_LIST);
    }

    /**
     *
     * @param fieldComponent field component
     * @param key name (key)
     * @param elseEmptyList  if result is null and elseEmptyList is true then it will return empty list
     * @return
     */
    public static List<Map<String, String>> getStringList(FieldComponent fieldComponent, String key, boolean elseEmptyList) {
        return Optional.ofNullable(getList(fieldComponent, key, elseEmptyList))
            .map(
                list ->
                    list.stream()
                        .map(FieldComponentUtil::toStringMap)
                        .collect(Collectors.toList())
            ).orElse(null);
    }

    /**
     * @param fieldComponent fieldComponent
     * @param key name (key)
     * @param elseEmptyList  if result is null and elseEmptyList is true then it will return empty list
     * @return List<Map<Object, Object>>
     */
    public static List<Map<Object, Object>> getList(FieldComponent fieldComponent, String key, boolean elseEmptyList) {
        return Optional.ofNullable(fieldComponent)
            .map(FieldComponent::getAttrs)
            .filter(attr -> attr.get(key) instanceof List)
            .map(attr -> (List<Object>) attr.get(key))
            .map(FieldComponentUtil::toMapList)
            .orElse(elseEmptyList ? ((List<Map<Object, Object>>) Collections.EMPTY_LIST) : null);
    }

    /**
     * List<Object> to List<Map<Object, Object>>
     * @param list list
     * @return List<Map<Object, Object>>
     */
    public static List<Map<Object, Object>> toMapList(List<Object> list) {
        return list
            .stream()
            .filter(item -> item instanceof Map)
            .map(item -> (Map<Object, Object>) item)
            .collect(Collectors.toList());
    }

    /**
     * List<Map<Object, Object>> to List<Map<String, String>>
     * @param list list
     * @return List<Map<String, String>>
     */
    public static List<Map<String, String>> toStringMapList(List<Map<Object, Object>> list) {
        return isNull(list)
            ? null
            : list
                .stream()
                .map(FieldComponentUtil::toStringMap)
                .collect(Collectors.toList());
    }

    /**
     * Map<Object, Object> to Map<String, String>
     * @param map list
     * @return Map<String, String>
     */
    public static Map<String, String> toStringMap(Map<? extends Object, ? extends Object> map) {
        return map
            .entrySet()
            .stream()
            .map(
                entry ->
                    new AbstractMap.SimpleEntry<String, String>(
                        entry.getKey() instanceof String ? (String) entry.getKey() : entry.getKey().toString(),
                        entry.getValue() instanceof String ? (String) entry.getValue() : (isNull(entry.getValue()) ? null : JsonProcessingUtil.toJson(entry.getValue()))
                    )
            )
            .collect(HashMap::new, (m,v)-> m.put(v.getKey(), v.getValue()), HashMap::putAll);
    }

    /**
     * Map<Object, Object> to Map<String, Object>
     * @param map list
     * @return Map<String, Object>
     */
    public static Map<String, Object> toStringKeyMap(Map<? extends Object, ? extends Object> map) {
        return map
            .entrySet()
            .stream()
            .map(
                entry ->
                    new AbstractMap.SimpleEntry<String, Object>(
                            entry.getKey() instanceof String ? String.valueOf(entry.getKey()) : entry.getKey().toString(),
                            entry.getValue()
                    )
            )
            .collect(HashMap::new, (m,v)-> m.put(v.getKey(), v.getValue()), HashMap::putAll);
    }


    public static List<String> getFieldNames(FieldComponent displayComponent) {
        List<Map<String, String>> fieldsList = FieldComponentUtil.getStringList(displayComponent, FieldComponentUtil.FIELDS_KEY, true);
        return fieldsList
                .stream()
                .flatMap(map -> map.entrySet().stream())
                .filter(e -> FIELD_NAME_KEY.equals(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public static List<FieldComponent> getFieldComponentsFromAttrs(FieldComponent fieldComponent, ServiceDescriptor serviceDescriptor) {
        Map<String, FieldComponent> supportedFields = serviceDescriptor.getApplicationFields().stream()
                .collect(Collectors.toMap(FieldComponent::getId, e -> e));
        return Optional.of(fieldComponent)
                .map(FieldComponent::getAttrs)
                .filter(attr -> attr.get(COMPONENTS_KEY) instanceof List)
                .map(attr -> (List<Object>) attr.get(COMPONENTS_KEY))
                .map(list -> list.stream()
                        .filter(v -> v instanceof String)
                        .map(supportedFields::get)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    /**
     * @param fieldComponent fieldComponent
     * @param key name (key)
     * @param elseEmptyMap  if result is null and elseEmptyMap is true then it will return empty map
     * @return Map<Object, Object>
     */
    public static Map<Object, Object> getAttrMap(FieldComponent fieldComponent, String key, boolean elseEmptyMap) {
        return Optional.ofNullable(fieldComponent)
                .map(FieldComponent::getAttrs)
                .filter(attr -> attr.get(key) instanceof Map)
                .map(attr -> (Map<Object, Object>) attr.get(key))
                .orElse(elseEmptyMap ? Collections.emptyMap() : null);
    }

    /**
     * @param fieldComponent fieldComponent
     * @param key name (key)
     * @return Map<String, String> or empty map
     */
    public static Map<String, String> getAttrStringMap(FieldComponent fieldComponent, String key) {
        return toStringMap(getAttrMap(fieldComponent, key, true));
    }

    /**
     * @param fieldComponent fieldComponent
     * @param key name (key)
     * @return Map<String, String> or null
     */
    public static Map<String, String> getAttrStringMapOrNull(FieldComponent fieldComponent,  String key) {
        return toStringMap(getAttrMap(fieldComponent, key, false));
    }

    public static void fillComponentErrorFromHint(FieldComponent component) {
        if (Objects.nonNull(component.getAttrs()) && component.getAttrs().get(DEFAULT_HINT) instanceof Map) {
            var hint = (Map<String, Object>) component.getAttrs().get(DEFAULT_HINT);
            if (!CollectionUtils.isEmpty(hint)) {
                FieldComponentError error = FieldComponentError.builder()
                        .type(ErrorType.fromStringOrDefault(String.valueOf(hint.get(HINT_TYPE)), ErrorType.error))
                        .title(String.valueOf(hint.get(HINT_TITLE)))
                        .desc(String.valueOf(hint.get(HINT_VALUE)))
                        .build();
                component.setErrors(List.of(error));
            }
        }
    }
}
