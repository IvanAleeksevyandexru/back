package ru.gosuslugi.pgu.components.descriptor.placeholder;

import lombok.Builder;
import lombok.Data;
import ru.gosuslugi.pgu.dto.descriptor.placeholder.Placeholder;

import java.util.Map;

/**
 * Контекст для получения значений в FormDto
 */
@Data
@Builder
public class PlaceholderContext {
    private Map<String, Placeholder> placeholderMap;
}
