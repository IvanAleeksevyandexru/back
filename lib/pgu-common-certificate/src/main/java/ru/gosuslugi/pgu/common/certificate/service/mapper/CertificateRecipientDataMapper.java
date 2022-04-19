package ru.gosuslugi.pgu.common.certificate.service.mapper;

import lombok.SneakyThrows;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.CertificateRecipientData;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.RegAddress;

import java.util.Map;

@Mapper
public abstract class CertificateRecipientDataMapper {
    public static final CertificateRecipientDataMapper INSTANCE = Mappers.getMapper(CertificateRecipientDataMapper.class);

    protected String genderString;
    protected RegAddress regAddress;
    protected String recipientBirthDate;

    @SneakyThrows
    @BeforeMapping
    protected void init(Map<String, String> componentArguments) {
        recipientBirthDate = CalendarConverter.formatDate(getFromMap(RecipientDataFields.BirthDate.name(), componentArguments));
        genderString = GenderNormalizer.normalizeGenderType(getFromMap(RecipientDataFields.GenderType.name(), componentArguments));
        String address = getFromMap(RecipientDataFields.recipientRegAddress.name(), componentArguments);
        regAddress = RegAddressMapper.INSTANCE.convert(address);
    }


    @Mapping(target = "recipientLastName", expression = "java(getFromMap(RecipientDataFields.LastName.name(), componentArguments))")
    @Mapping(target = "recipientFirstName", expression = "java(getFromMap(RecipientDataFields.FirstName.name(), componentArguments))")
    @Mapping(target = "recipientPatronymic", expression = "java(getFromMap(RecipientDataFields.MiddleName.name(), componentArguments))")
    @Mapping(target = "recipientBirthDate", expression = "java(recipientBirthDate)")
    @Mapping(target = "recipientSNILS", expression = "java(getFromMap(RecipientDataFields.SNILS.name(), componentArguments))")
    @Mapping(target = "recipientGenderType", expression = "java(genderString)")
    @Mapping(target = "recipientBirthCertificateData", expression = "java(BirthCertificateDataTypeMapper.INSTANCE.convert(componentArguments))")
    @Mapping(target = "recipientForeignBirthCertificateData", expression = "java(ForeignBirthCertificateDataTypeMapper.INSTANCE.convert(componentArguments))")
    @Mapping(target = "recipientRegAddress", expression = "java(regAddress)")
    public abstract CertificateRecipientData convert(Map<String, String> componentArguments);

    @AfterMapping
    protected void cleanBirthdayCertificateData(Map<String, String> componentArguments, @MappingTarget CertificateRecipientData certificateRecipientData) {
        if (Boolean.parseBoolean(componentArguments.get("isForeign"))) {
            certificateRecipientData.setRecipientBirthCertificateData(null);
        } else {
            certificateRecipientData.setRecipientForeignBirthCertificateData(null);
        }
    }

    protected String getFromMap(String key, Map<String, String> componentArguments) {
        return componentArguments.getOrDefault(key, "");
    }

    protected enum RecipientDataFields {
        LastName,
        FirstName,
        MiddleName,
        BirthDate,
        SNILS,
        GenderType,
        recipientRegAddress
    }

}
