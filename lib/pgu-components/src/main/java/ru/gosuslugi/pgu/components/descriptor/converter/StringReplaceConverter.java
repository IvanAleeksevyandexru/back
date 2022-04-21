package ru.gosuslugi.pgu.components.descriptor.converter;

import org.apache.logging.log4j.util.Strings;

import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Replace subsrtings by regex
 */
public class StringReplaceConverter implements Converter {

    private static final String REPLACEMENT_ATTR = "for";

    @Override
    public Object convert(Object value, Map<String, Object> attrs) {
        String result = Optional.ofNullable(value).map(Object::toString).orElse(Strings.EMPTY);
        var optional = Optional.ofNullable(attrs.get(REPLACEMENT_ATTR));
        if (isBlank(result) || optional.isEmpty()) {
            return result;
        }

        String[] res = new String[] {result};

        if (optional.get() instanceof Map) {
            ((Map<String, String>) optional.get())
                    .forEach((k, v) -> res[0] = res[0].replaceAll(k, v));
        }

        return res[0];
    }
}
