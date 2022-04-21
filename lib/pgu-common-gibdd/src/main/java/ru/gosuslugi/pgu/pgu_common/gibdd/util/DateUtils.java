package ru.gosuslugi.pgu.pgu_common.gibdd.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
public class DateUtils {

    private static final DateTimeFormatter FROM_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FROM_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TO_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter INPUT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");


    public static String formatDate(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }

        boolean isDate = value.length() == 10;
        try {
            if (isDate) {
                return LocalDate.parse(value, FROM_DATE_FORMATTER).format(TO_FORMATTER);
            }

            return LocalDateTime.parse(value, FROM_DATETIME_FORMATTER).format(TO_FORMATTER);
        } catch (DateTimeException e) {
            log.error("Ошибка преобразования даты", e);
        }

        return value;
    }

    public static Object format(Object format, String value) {
        try {
            if ("utc_date:dd.MM.yyyy".equals(format)) {
                return TO_FORMATTER.format(INPUT_DATE_TIME_FORMATTER.parse(value));
            }
        } catch ( DateTimeParseException e) {
            if (log.isWarnEnabled()) {
                log.warn("Не распарсилась дата \"" + value + "\" в формате \"yyyy-MM-dd'T'HH:mm:ss.SSSXXX\"", e);
            }
            return value;
        }
        return value;
    }
}
