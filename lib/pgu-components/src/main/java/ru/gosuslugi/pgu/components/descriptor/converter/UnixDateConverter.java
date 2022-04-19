package ru.gosuslugi.pgu.components.descriptor.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Map;

@Slf4j
public class UnixDateConverter implements Converter {

    static final String FORMAT_ATTR = "format";
    static final String DD_MM_YYYY = "dd.MM.yyyy";

    @Override
    public String convert(Object value, Map<String, Object> attrs) {
        if (StringUtils.isEmpty(value)) return "";

        String dateValueString = value.toString();
        long unixDateValue = Long.parseLong(dateValueString);
        String dateFormat = attrs.getOrDefault(FORMAT_ATTR, DD_MM_YYYY).toString();

        try {
            Temporal dateTime = LocalDateTime.ofEpochSecond(unixDateValue, 0, ZoneOffset.UTC);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
            return formatter.format(dateTime);
        } catch (IllegalArgumentException | DateTimeException ex) {
            log.warn("Error by date \"{}\" parsing dateTime. Details: {}", dateValueString, ex.getMessage());
        }

        return dateValueString;
    }

}
