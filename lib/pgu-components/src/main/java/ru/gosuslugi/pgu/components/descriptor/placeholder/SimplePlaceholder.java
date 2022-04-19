package ru.gosuslugi.pgu.components.descriptor.placeholder;

import com.jayway.jsonpath.DocumentContext;
import lombok.Data;
import ru.gosuslugi.pgu.dto.descriptor.placeholder.Placeholder;
import ru.gosuslugi.pgu.dto.descriptor.placeholder.PlaceholderType;

@Data
public class SimplePlaceholder extends Placeholder {
    private String value;

    @Override
    public PlaceholderType getType() {
        return PlaceholderType.simple;
    }

    @Override
    public String getNext(DocumentContext... contexts) {
        return value;
    }
}
