package ru.gosuslugi.pgu.components.descriptor.placeholder;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.Data;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.core.date.util.DateUtil;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.dto.descriptor.placeholder.Placeholder;
import ru.gosuslugi.pgu.dto.descriptor.placeholder.PlaceholderType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Реф для подстановки значения
 */
@Data
public class Reference extends Placeholder {

    public static final int FIELD_ID_INDEX_IN_PATH = 0;
    public static final int VALUE_OR_VISITED_INDEX_IN_PATH = 1;

    /** json-path для поиска значения */
    private String path;

    /** для сохранения найденого значения в рамках заданного контекста */
    private Map<DocumentContext[], Object> valueMap = new HashMap<>();

    /** атрибуты рефа */
    private ReferenceAttrs attrs;

    @Override
    public PlaceholderType getType() {
        return PlaceholderType.ref;
    }

    @Override
    public String getNext(DocumentContext... contexts) {
        return (String) valueMap.computeIfAbsent(contexts, val -> getStringValueForField(path, contexts));
    }

    private String getStringValueForField(String field, DocumentContext... contexts) {
        Optional<String> valueOptional = getFromContext(field, contexts);
        String value = field;
        if (valueOptional.isPresent()) {
            value = valueOptional.get();
            if (attrs != null) {
                value = (String) attrs.getConverterType().getConverter().convert(value, attrs.getAttrs());
            }
            return DateUtil.checkForDateTime(value);
        }
        if (attrs != null) {
            value = attrs.getConverterType().getConverter().getDefaultValue(attrs.getAttrs()).orElse(field);
        }

        return value;
    }

    // TODO вынести в Util
    private Optional<String> getFromContext(String field, DocumentContext... contexts) {
        if (!StringUtils.hasText(field)) {
            return Optional.empty();
        }

        for(DocumentContext context: contexts) {
            String value = getFieldFromContext(field, context);
            if (value != null) {
                return Optional.of(value);
            }
            String pathSubstring = getSubPath(field);
            value = getFieldFromContext(pathSubstring, context);
            if (value != null) {
                return Optional.of("");
            }
        }
        return Optional.empty();
    }

    // TODO вынести в Util
    private String getFieldFromContext(String field, DocumentContext documentContext) {
        try {
            Object objectValue =  documentContext.read("$." + field, Object.class);
            return StringUtils.isEmpty(objectValue) ? "" : JsonProcessingUtil.toJson(objectValue);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    private String getSubPath(String reference) {
        String[] pathArr = reference.split("\\.");
        String fieldId = pathArr[FIELD_ID_INDEX_IN_PATH];
        String valueOrVisited = pathArr.length > 1 ? pathArr[VALUE_OR_VISITED_INDEX_IN_PATH] : "value";
        return String.format("%s.%s", fieldId, valueOrVisited);
    }
}
