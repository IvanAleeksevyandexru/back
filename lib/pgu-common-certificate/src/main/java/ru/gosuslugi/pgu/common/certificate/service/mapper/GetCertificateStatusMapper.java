package ru.gosuslugi.pgu.common.certificate.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.GetCertificateStatus;

import java.util.Map;

@Mapper
public abstract class GetCertificateStatusMapper {

    public static final GetCertificateStatusMapper INSTANCE = Mappers.getMapper(GetCertificateStatusMapper.class);

    @Mapping(target = "certificateRecipientData", expression = "java(CertificateRecipientDataMapper.INSTANCE.convert(componentArguments))")
    @Mapping(target = "applicantData", expression = "java(ApplicantDataMapper.INSTANCE.convert(componentArguments))")
    @Mapping(target = "applicationDetails", expression = "java(ApplicationDetailsMapper.INSTANCE.convert(componentArguments))")
    public abstract GetCertificateStatus convert(Map<String, String> componentArguments) ;

}
