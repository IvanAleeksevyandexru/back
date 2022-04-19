package ru.gosuslugi.pgu.components.descriptor.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.*;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Конвертор даты
 */
@Slf4j
public class DateConverter implements Converter {

    private final static String FORMAT_ATTR = "format";
    private final static String INPUT_FORMAT_ATTR = "inputDateFormat";
    private final static String LOCALE_ATTR = "locale";
    private final static String TIMEZONE_ATTR = "timezone";
    private final static String DEFAULT_LOCALE = "ru";
    private final static Set<String> TIME_FORMAT_LITERALS = Set.of("u", "a", "H", "k", "K", "h", "m", "s", "S", "z", "Z", "X");

    @Override
    public String convert(Object value, Map<String, Object> attrs) {
        String date = Optional.ofNullable(value).map(Object::toString).orElse("");
        if (isBlank(date) || !attrs.containsKey(FORMAT_ATTR)) {
            return date;
        }
        String formatString = attrs.get(FORMAT_ATTR).toString();
        Locale locale = getLocale(attrs);
        try {
            TemporalAccessor dateTime = getTemporalAccessorByInputDateFormat(attrs, locale, date);
            if(dateTime == null) {
                String timezone = (String) attrs.get(TIMEZONE_ATTR);
                dateTime = parseDateTime(date, formatString, timezone);
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatString, locale);
            return formatter.format(dateTime);
        } catch (DateTimeParseException ex) {
            if (log.isWarnEnabled()) log.warn("Error by date \"{}\" parsing dateTime. Details: {}", date, ex.getMessage());
        } catch (IllegalArgumentException | DateTimeException ex) {
            if (log.isWarnEnabled()) log.warn("Error by date \"{}\" format of pattern \"{}\". Details: {}", date, attrs.get(FORMAT_ATTR).toString(), ex.getMessage());
        }
        return date;
    }

    private TemporalAccessor getTemporalAccessorByInputDateFormat(Map<String, Object> attrs, Locale locale, String date) {
        if (attrs.containsKey(INPUT_FORMAT_ATTR)) {
            try {
                String inputFormatString = attrs.get(INPUT_FORMAT_ATTR).toString();
                return DateTimeFormatter.ofPattern(inputFormatString, locale).parse(date);
            } catch (DateTimeParseException ex) {
                if (log.isWarnEnabled())
                    log.warn("Error by date \"{}\" parsing to input format. Details: {}", date, ex.getMessage());
            }
        }
        return null;
    }

    private TemporalAccessor parseDateTime(String date, String formatString, String timezone) {
        TemporalAccessor dateTime = parseOffsetDateTime(date);

        if (dateTime != null) {
            if (isFormatContainsTimePart(formatString) && !StringUtils.isEmpty(timezone)) {
                dateTime = ((OffsetDateTime) dateTime).atZoneSameInstant(ZoneId.of(timezone)).toOffsetDateTime();
            }
        } else {
            // todo: для обратной совместимости с тем что уже есть в черновиках пробуем спарсить в LocalDateTime
            dateTime = LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        return dateTime;
    }

    private Locale getLocale(Map<String, Object> attrs) {
        return Optional.ofNullable(attrs.get(LOCALE_ATTR))
                .map(locale ->  (String) locale)
                .map(Locale::forLanguageTag)
                .orElse(new Locale(DEFAULT_LOCALE));
    }

    private OffsetDateTime parseOffsetDateTime(String value) {
        try {
            return OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException ex) {
            if (log.isWarnEnabled()) log.warn("Error by date \"{}\" parsing  to OffsetDateTime class. Details: {}", value, ex.getMessage());
        }

        return null;
    }

    private boolean isFormatContainsTimePart(String format) {
        return Objects.nonNull(format) && TIME_FORMAT_LITERALS.stream().anyMatch(format::contains);
    }
}
