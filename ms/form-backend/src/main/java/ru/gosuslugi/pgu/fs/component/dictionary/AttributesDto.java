package ru.gosuslugi.pgu.fs.component.dictionary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

public interface AttributesDto<T> {

    ObjectMapper mapper = JsonProcessingUtil.getObjectMapper();

    default T getAttributesDto(FieldComponent component) {
        return mapper.convertValue(component.getAttrs(), getAttributesDtoType());
    }

    TypeReference<T> getAttributesDtoType();
}
