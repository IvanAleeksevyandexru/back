package ru.gosuslugi.pgu.components.descriptor.converter;

import java.util.Map;
import java.util.Optional;

import static ru.gosuslugi.pgu.components.descriptor.converter.DateConverter.DEFAULT_ATTR;

/**
 * Интерфейс конвертора для рефов
 */
public interface Converter {
    String DEFAULT_ATTR = "default";

    Object convert(Object value, Map<String, Object> attrs);

    default Optional<String> getDefaultValue(Map<String, Object> attrs) {
        return Optional.ofNullable(attrs.get(DEFAULT_ATTR)).map(Object::toString);
    }
}
