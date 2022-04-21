package ru.gosuslugi.pgu.common.certificate.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.BirthCertificateDataType;

import java.util.Map;

@Mapper
public abstract class BirthCertificateDataTypeMapper extends RecipientBirthCertificateData {

    public static final BirthCertificateDataTypeMapper INSTANCE = Mappers.getMapper(BirthCertificateDataTypeMapper.class);

    @Mapping(target = "recipientBirthCertificateAgency", expression = "java(agency)")
    @Mapping(target = "recipientBirthCertificateIssueDate", expression = "java(issueDate)")
    @Mapping(target = "recipientBirthCertificateSeries", expression = "java(series)")
    @Mapping(target = "recipientBirthCertificateNumber", expression = "java(number)")
    public abstract BirthCertificateDataType convert(Map<String, String> componentArguments);
}
