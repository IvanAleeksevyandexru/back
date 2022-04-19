package ru.gosuslugi.pgu.common.certificate.service.mapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.GregorianCalendar;

@Slf4j
public class CalendarConverter {
    private final static String EUROPEAN_DATE_PATTERN = "dd.MM.yyyy";
    private final static DateTimeFormatter EUROPEAN_DATE_FORMATTER = DateTimeFormatter.ofPattern(EUROPEAN_DATE_PATTERN);

    private final static String ISO_DATE_PATTERN = "yyyy-MM-dd";
    private final static DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern(ISO_DATE_PATTERN);

    public static ZonedDateTime convertToZoneDateTime(String dt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(""
                + "[yyyy-MM-dd['T'][ ][HH:mm:ss][.SSS]['Z'][ ][Z]]"
                + "[dd.MM.yyyy[ HH:mm:ss][.SSS][ ][Z]]"
        );
        ZonedDateTime zonedDateTime;
        if (dt.length() <= 10) {
            LocalDate localDate = LocalDate.parse(dt, formatter);
            zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
        } else {
            if (dt.contains(" +") || dt.contains(" -")) {
                zonedDateTime = ZonedDateTime.parse(dt, formatter);
            }else {
                LocalDateTime localDateTime = LocalDateTime.parse(dt, formatter);
                zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
            }
        }
        return zonedDateTime;
    }

    public static String formatDate(String dt) {
        if (StringUtils.isEmpty(dt)) {
            return dt;
        }
        LocalDate localDate = null;
        try {
            localDate = LocalDate.parse(dt, EUROPEAN_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.info(String.format("Дата %s не распарсилась в формат %s", dt, EUROPEAN_DATE_PATTERN));
        }
        try {
            localDate = LocalDate.parse(dt, ISO_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.info(String.format("Дата %s не распарсилась в формат %s", dt, ISO_DATE_PATTERN));
        }
        if (localDate == null) {
            log.error(String.format("Дата %s не распарсилась в формат %s", dt, ISO_DATE_PATTERN));
            return "";
        }
        return localDate.format(ISO_DATE_FORMATTER);
    }

    public static XMLGregorianCalendar convertToXMLGregorianCalendar(String dt) throws DatatypeConfigurationException {

        if (dt.isEmpty())
            return null;
        ZonedDateTime zonedDateTime = convertToZoneDateTime(dt);
        GregorianCalendar gregorianCalendar = GregorianCalendar.from(zonedDateTime);
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
    }

}
