package ru.gosuslugi.pgu.components.descriptor.placeholder;

import lombok.Data;
import ru.gosuslugi.pgu.components.descriptor.converter.ConverterType;

import java.util.Map;

/**
 * Атрибуты рефа
 * @see Reference
 */
@Data
public class ReferenceAttrs {
    private ConverterType converterType;
    /** кастомные атрибуты */
    private Map<String, Object> attrs;
}
