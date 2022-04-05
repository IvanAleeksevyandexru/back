package ru.gosuslugi.pgu.components;


import ru.gosuslugi.pgu.components.dto.AddressType;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.components.dto.FieldDto;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Common util static methods for pre-setting fields
 */
public class BasicComponentUtil {
    /**
     * Name of attribute to define specification of used fields of complex components
     */
    public static String PRESET_ATTR_NAME = "fields";

    /** Ключ типа адреса, который используется в сценарии. Пример: "attrs": {"addrType": "permanentRegistry"... */
    public static String PRESET_ADDR_TYPE = "addrType";


    /**
     * Name of field that should be used for frontend
     */
    public static String PRESET_FIELD_NAME_ATTR_NAME = "fieldName";
    public static String PRESET_FIELD_LABEL_ATTR_NAME = "label";
    public static String PRESET_FIELD_RANK_ATTR_NAME = "rank";
    public static String PRESET_FIELD_REQUIRED_ATTR_NAME = "required";

    public static Set<String> getPreSetFields(FieldComponent component) {
        Set<String> fields = new HashSet<>();
        if(component.getAttrs().containsKey(PRESET_ATTR_NAME)) {
            ArrayList<LinkedHashMap> filedsConfig = (ArrayList<LinkedHashMap>) component.getAttrs().get(PRESET_ATTR_NAME);
            fields = filedsConfig.stream().map(x -> (String) x.get(PRESET_FIELD_NAME_ATTR_NAME)).collect(Collectors.toSet());
        }
        return fields;
    }

    public static Map<String, FieldDto> getFieldNameToFieldDtoMap(FieldComponent component) {
        Map<String, FieldDto> fields = Collections.emptyMap();
        if(component.getAttrs().containsKey(PRESET_ATTR_NAME)) {
            ArrayList<LinkedHashMap> fieldsList = (ArrayList<LinkedHashMap>) component.getAttrs().get(PRESET_ATTR_NAME);
            fields = fieldsList.stream().collect(Collectors.toMap(map -> map.get(PRESET_FIELD_NAME_ATTR_NAME).toString(),
                    map -> FieldDto.builder()
                            .label(map.getOrDefault(PRESET_FIELD_LABEL_ATTR_NAME, "").toString())
                            .rank(Objects.isNull(map.get(PRESET_FIELD_RANK_ATTR_NAME)) ? null : Boolean.valueOf(map.get(PRESET_FIELD_RANK_ATTR_NAME).toString()))
                            .required(Objects.isNull(map.get(PRESET_FIELD_REQUIRED_ATTR_NAME)) ? false : Boolean.valueOf(map.get(PRESET_FIELD_REQUIRED_ATTR_NAME).toString()).booleanValue())
                            .build()

            ));
        }
        return fields;
    }

    public static Map<String, FieldDto> getComponentFields(FieldComponent component) {
        if (Objects.isNull(component.getAttrs())
                || !component.getAttrs().containsKey(PRESET_ATTR_NAME)
                || !(component.getAttrs().get(PRESET_ATTR_NAME) instanceof List)) {
            return Collections.emptyMap();
        }

        var fieldsList = (ArrayList<LinkedHashMap<String, Object>>) component.getAttrs().get(PRESET_ATTR_NAME);

        return fieldsList.stream()
                .collect(Collectors.toMap(
                        map -> map.get(PRESET_FIELD_NAME_ATTR_NAME).toString(),
                        map -> FieldDto.builder()
                                .label(map.getOrDefault(PRESET_FIELD_LABEL_ATTR_NAME, "").toString())
                                .rank(Objects.isNull(map.get(PRESET_FIELD_RANK_ATTR_NAME)) ? null : Boolean.valueOf(map.get(PRESET_FIELD_RANK_ATTR_NAME).toString()))
                                .required(!Objects.isNull(map.get(PRESET_FIELD_REQUIRED_ATTR_NAME)) && Boolean.parseBoolean(map.get(PRESET_FIELD_REQUIRED_ATTR_NAME).toString()))
                                .attrs(map.containsKey("attrs") ? (Map<String, Object>) map.get("attrs") : new HashMap<>())
                                .build()
                ));
    }

    /**
     * Безопасное вычисление типа адреса
     * @param component компонент с ожидаемым ключем типа адреса в атрибутах
     * @return AddressType объект или null
     */
    public static AddressType getAddrType(FieldComponent component) {
        return Optional.ofNullable(component.getAttrs())
                .map(map -> map.get(PRESET_ADDR_TYPE))
                .map(Object::toString)
                .map(AddressType::fromString)
                .orElse(null);
    }
}
