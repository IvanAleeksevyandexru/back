package ru.gosuslugi.pgu.common.certificate.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.ForeignBirthCertificateDataType;

import java.util.Map;

@Mapper
public abstract class ForeignBirthCertificateDataTypeMapper extends RecipientBirthCertificateData {

    public static final ForeignBirthCertificateDataTypeMapper INSTANCE = Mappers.getMapper(ForeignBirthCertificateDataTypeMapper.class);

    @Mapping(target = "recipientForeignBirthCertificateAgency", expression = "java(agency)")
    @Mapping(target = "recipientForeignBirthCertificateIssueDate", expression = "java(issueDate)")
    @Mapping(target = "recipientForeignBirthCertificateSeries", expression = "java(series)")
    @Mapping(target = "recipientForeignBirthCertificateNumber", expression = "java(number)")
    public abstract ForeignBirthCertificateDataType convert(Map<String, String> componentArguments);
}
