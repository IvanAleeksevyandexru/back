package ru.gosuslugi.pgu.common.sop.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Элемент с различными атрибутами из справочника.
 * Используется map, т.к. в разных справочниках в ответе возвращаются разные атрибуты.
 * Только клиент (конкретный компонент или сервис) знает, какие атрибуты нужно доставать из map.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SopResponseDataItem {

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

	public String getAttributeValue(String key) {
		if (attributeValues == null) {
			return null;
		}
		Object obj = attributeValues.get(key);
		return obj == null ? null : obj.toString();
	}
}