package ru.gosuslugi.pgu.common.certificate.service.mapper;

import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.ApplicationDetails;

import java.util.Map;

@Mapper
public abstract class ApplicationDetailsMapper {

    public final static ApplicationDetailsMapper INSTANCE = Mappers.getMapper(ApplicationDetailsMapper.class);
    protected final static String PFDOD_CERTIFICATE = "pfdod_certificate";

    protected String payment;

    @BeforeMapping
    protected void init(Map<String, String> componentArguments) {
        payment = componentArguments.getOrDefault("payment", "");
    }

    protected String getFromMap(String key, Map<String, String> componentArguments) {
        return componentArguments.getOrDefault(key, "");
    }

    @Mapping(target = "municipalityCode", expression = "java(getFromMap(ApplicantDataMapper.ApplicantFields.municipalityCode.name(),componentArguments))")
    @Mapping(target = "regionCode", expression = "java(getFromMap(ApplicantDataMapper.ApplicantFields.regionCode.name(),componentArguments))")
    @Mapping(target = "municipalityName", expression = "java(getFromMap(ApplicantDataMapper.ApplicantFields.municipalityName.name(),componentArguments))")
    @Mapping(target = "regionName", expression = "java(getFromMap(ApplicantDataMapper.ApplicantFields.regionName.name(),componentArguments))")
    /**
     * Тип оплаты группы
     * Значение из списка {budget, none, paid, private, pfdod_certificate}
     * Если тип pfdod_certificate, то сделать запрос с isFundedCertificate=true
     * Если тип !pfdod_certificate, то сделать запрос с isFundedCertificate=false
     */
    @Mapping(target = "isFundedCertificate", expression = "java(PFDOD_CERTIFICATE.equals(payment))")
    public abstract ApplicationDetails convert(Map<String, String> componentArguments) ;
}
