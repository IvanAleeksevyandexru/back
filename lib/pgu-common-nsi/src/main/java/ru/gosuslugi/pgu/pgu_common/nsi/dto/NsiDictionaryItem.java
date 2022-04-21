package ru.gosuslugi.pgu.pgu_common.nsi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

/**
 * Item in NSI dictionary
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class NsiDictionaryItem {
    String value;
    String title;
    Map<String, String> attributeValues;

    public String getAttributeValue(String key) {
        if (attributeValues == null) {
            return null;
        }

        return attributeValues.get(key);
    }
}
