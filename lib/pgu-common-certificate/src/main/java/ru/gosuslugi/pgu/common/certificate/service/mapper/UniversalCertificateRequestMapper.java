package ru.gosuslugi.pgu.common.certificate.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.UniversalCertificateRequest;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

@Mapper
public abstract class UniversalCertificateRequestMapper {

    @Mapping(target = "orderId", expression = "java(Long.parseLong(orderId))")
    @Mapping(target = "orderTimestamp", expression = "java(calcCurrentDateTime())")
    @Mapping(target = "getCertificateStatusRequest", expression = "java(GetCertificateStatusMapper.INSTANCE.convert(componentArguments))")
    public abstract UniversalCertificateRequest convert(Map<String, String> componentArguments, String orderId);

    protected Calendar calcCurrentDateTime() {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        return GregorianCalendar.from(zdt);
    }


}
