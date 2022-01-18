package ru.gosuslugi.pgu.components.descriptor.placeholder;

import com.jayway.jsonpath.DocumentContext;
import lombok.Data;
import ru.gosuslugi.pgu.dto.descriptor.placeholder.Placeholder;
import ru.gosuslugi.pgu.dto.descriptor.placeholder.PlaceholderType;

/**
 * Атрибуты для которых нужна особая обработка
 */
@Data
public class SequentialPlaceholder extends Placeholder {
    /** Тип  */
    private int value = 0;

    @Override
    public PlaceholderType getType() {
        return PlaceholderType.sequential;
    }

    @Override
    public String getNext(DocumentContext... contexts) {
        return String.valueOf(++value);
    }
}
