package ru.gosuslugi.pgu.common.certificate.converters;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import ru.gosuslugi.pgu.common.certificate.service.mapper.*;

import java.time.ZonedDateTime;

@Slf4j
public class CertificateRecipientDataConverterTest {

    @Test
    public void convertToXMLGregorianCalendar() {
        String gettingString = "2021-06-28 14:25:05Z";
        ZonedDateTime zonedDateTime = CalendarConverter.convertToZoneDateTime(gettingString);
        log.info("zonedDateTime == {}", zonedDateTime);
        Assert.assertEquals(14, zonedDateTime.getHour());
        Assert.assertEquals(25, zonedDateTime.getMinute());
        gettingString = "2021-06-28 14:25:05 +0300";
        zonedDateTime = CalendarConverter.convertToZoneDateTime(gettingString);
        log.info("zonedDateTime == {}", zonedDateTime);
        Assert.assertEquals(14, zonedDateTime.getHour());
        Assert.assertEquals(25, zonedDateTime.getMinute());
    }
}