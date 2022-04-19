package ru.gosuslugi.pgu.components.descriptor.types;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Кастомные ссылки (на всё, кроме ApplicantAnswers)
 */
public enum PresetRefAttrsType {
    REF_TO_ORDER_ID("reference_to_order_id"),
    REF_TO_MASTER_ORDER_ID("reference_to_master_order_id"),
    REF_TO_CURRENT_DATE("reference_to_current_date");

    private final String attrName;

    public String getAttrName() {
        return attrName;
    }

    PresetRefAttrsType(String attrName) {
        this.attrName = attrName;
    }

    public static List<String> getAttrNames() {
        return Arrays.stream(PresetRefAttrsType.values()).map(PresetRefAttrsType::getAttrName).collect(Collectors.toList());
    }

    private static final Map<String, PresetRefAttrsType> mapVal = new HashMap<>();

    static {
        for (PresetRefAttrsType value : PresetRefAttrsType.values()) {
            mapVal.put(value.getAttrName(), value);
        }
    }

    public static PresetRefAttrsType get(String attrName) {
        return mapVal.get(attrName);
    }
}
