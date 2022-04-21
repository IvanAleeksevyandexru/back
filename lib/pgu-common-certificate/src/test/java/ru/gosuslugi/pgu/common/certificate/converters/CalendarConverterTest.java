package ru.gosuslugi.pgu.common.certificate.converters;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import ru.gosuslugi.pgu.common.certificate.service.mapper.CalendarConverter;
import ru.gosuslugi.pgu.common.certificate.service.mapper.GenderNormalizer;

import javax.xml.datatype.XMLGregorianCalendar;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


@Slf4j
public class CalendarConverterTest {
    private CalendarConverter calendarConverter = new CalendarConverter() {
    };

    private GenderNormalizer genderNormalizer = new GenderNormalizer() {
    };
    @Test
    public void multFormatTest(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(""
                + "[yyyy-MM-dd[ HH:mm:ss][.SSSSSS][ ][Z]]"
                + "[dd.MM.yyyy[ HH:mm:ss][.SSS][ ][Z]]"
        );

        log.info("{}", ZonedDateTime.parse("2016-03-23 22:00:00.256145 +0300", formatter));
        log.info("{}", ZonedDateTime.parse("2016-03-23 22:00:00 +0300", formatter));
        log.info("{}", ZonedDateTime.parse("2016-03-23 22:00:00.256145+0300", formatter));
        log.info("{}", ZonedDateTime.parse("23.03.2016 22:00:00.145+0300", formatter));
        log.info("{}", ZonedDateTime.parse("2016-03-23 22:00:00+0300", formatter));
        log.info("{}", LocalDate.parse("2016-03-23", formatter));
    }

    @SneakyThrows
    @Test
    public void convertToXMLGregorianCalendar_validData() {
        XMLGregorianCalendar dateTime = calendarConverter.convertToXMLGregorianCalendar("2021-06-30 11:12:13 +0300");
        Assert.assertEquals(2021, dateTime.getYear());
        Assert.assertEquals(6, dateTime.getMonth());
        XMLGregorianCalendar dateOnly = calendarConverter.convertToXMLGregorianCalendar("2021-06-30");
        Assert.assertEquals(2021, dateOnly.getYear());
        Assert.assertEquals(6, dateOnly.getMonth());
        Assert.assertEquals(30, dateOnly.getDay());


    }

    @SneakyThrows
    @Test
    public void convertToXMLGregorianCalendar_emptyData() {
        XMLGregorianCalendar xmlGregorianCalendar = calendarConverter.convertToXMLGregorianCalendar("");
        Assert.assertNull(xmlGregorianCalendar);
    }

    @Test
    public void normalizeGenderType() {
        Assert.assertEquals("Male", genderNormalizer.normalizeGenderType("муж"));
        Assert.assertEquals("Female", genderNormalizer.normalizeGenderType("жен"));
        Assert.assertEquals("Female", genderNormalizer.normalizeGenderType("Ж"));
        Assert.assertEquals("Female", genderNormalizer.normalizeGenderType("female"));
        Assert.assertEquals("Female", genderNormalizer.normalizeGenderType("F"));
    }
}