package ru.gosuslugi.pgu.components.descriptor.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
public class ToUnixDateConverter implements Converter {

    static final String FORMAT_ATTR = "format";
    public static final String DEFAULT_TIME_PART = "T00:00:00.000";

    @Override
    public String convert(Object value, Map<String, Object> attrs) {
        if (StringUtils.isEmpty(value)) return "";

        var formatter = DateTimeFormatter.ISO_DATE_TIME;
        String dateValueString = value.toString();

        try {
            if (attrs.containsKey(FORMAT_ATTR))
                formatter = DateTimeFormatter.ofPattern(attrs.get(FORMAT_ATTR).toString());
            else if (dateValueString.length() < 11)
                dateValueString += DEFAULT_TIME_PART;

            LocalDateTime dateTime = LocalDateTime.parse(dateValueString, formatter);
            return String.valueOf(dateTime.toEpochSecond(ZoneOffset.UTC));
        } catch (IllegalArgumentException | DateTimeException ex) {
            log.warn("Error by date \"{}\" convert to Unix datetime. Details: {}", dateValueString, ex.getMessage());
        }
        return dateValueString;
    }

}
