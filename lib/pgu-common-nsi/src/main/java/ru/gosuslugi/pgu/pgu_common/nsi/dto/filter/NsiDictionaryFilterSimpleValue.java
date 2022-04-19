package ru.gosuslugi.pgu.pgu_common.nsi.dto.filter;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class NsiDictionaryFilterSimpleValue {
    /** Список всех атрибутов с произвольными именами и значениями. */
    private Map<String, Object> attributeValues;

    @JsonAnyGetter
    public Map<String, Object> getAttributeValues() {
        return attributeValues;
    }

    @JsonAnySetter
    public void putAttributeValue(String key, Object value) {
        if (attributeValues == null) {
            attributeValues = new HashMap<>();
        }
        attributeValues.put(key, value);
    }

    public Object getAttributeValue(String key) {
        return attributeValues == null ? null : attributeValues.get(key);
    }
}