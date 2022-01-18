package ru.gosuslugi.pgu.fs.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@UtilityClass
public class DateTimeUtil {

    public LocalDate parseDate(String date, String format) {
        try {
            val dateFormatter = DateTimeFormatter.ofPattern(format);
            return LocalDate.parse(date, dateFormatter);
        } catch (IllegalArgumentException | DateTimeParseException e) {
            log.error("Date parsing error", e);
            return null;
        }
    }
}