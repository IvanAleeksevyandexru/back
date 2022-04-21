package ru.gosuslugi.pgu.common.certificate.service.mapper;

import lombok.SneakyThrows;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.gosuslugi.pgu.common.certificate.dto.PersonCertEaisdoDto;
import ru.gosuslugi.pgu.common.certificate.dto.PersonStoreValuesDto;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.ApplicantData;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;

import java.util.Map;

@Mapper(imports = {JsonProcessingUtil.class})
public abstract class ApplicantDataMapper  {

    public static final ApplicantDataMapper INSTANCE = Mappers.getMapper(ApplicantDataMapper.class);

    protected PersonStoreValuesDto storedValues;
    protected String regAddressString;
    protected String applicantBirthDate;

    @SneakyThrows
    @BeforeMapping
    protected void init(Map<String, String> componentArguments) {
        String applicantJson = componentArguments.getOrDefault(ApplicantFields.applicantData.value, "");
        PersonCertEaisdoDto applicantPerson = JsonProcessingUtil.fromJson(applicantJson, PersonCertEaisdoDto.class);
        storedValues = applicantPerson.getStoredValues();
        regAddressString = componentArguments.getOrDefault(ApplicantFields.applicantRegAddress.value, "");
        applicantBirthDate = CalendarConverter.formatDate(storedValues.getBirthDate());
    }

    @Mapping(target = "applicantLastName", expression = "java(storedValues.getLastName())")
    @Mapping(target = "applicantFirstName", expression = "java(storedValues.getFirstName())")
    @Mapping(target = "applicantPatronymic", expression = "java(storedValues.getMiddleName())")
    @Mapping(target = "applicantRegAddress", expression = "java(RegAddressMapper.INSTANCE.convert(regAddressString))")
    @Mapping(target = "applicantPassportData", expression = "java(PassportDataTypeMapper.INSTANCE.convert(componentArguments))")
    @Mapping(target = "applicantBirthDate", expression = "java(applicantBirthDate)")
    @Mapping(target = "applicantPhoneNumber", expression = "java(getFromMap(ApplicantFields.phone.value, componentArguments))")
    @Mapping(target = "applicantEmail", expression = "java(getFromMap(ApplicantFields.email.value, componentArguments))")
    public abstract ApplicantData convert(Map<String, String> componentArguments);

    enum ApplicantFields {
        applicantData("applicantData"),
        phone("applicantPhoneNumber"),
        email("applicantEmail"),
        applicantRegAddress("applicantRegAddress"),
        municipalityCode("municipalityCode"),
        municipalityName("municipalityName"),
        regionCode("regionCode"),
        regionName("regionName");


        public final String value;

        ApplicantFields(String value) {
            this.value = value;
        }
    }

    protected String getFromMap(String key, Map<String, String> componentArguments) {
        return componentArguments.getOrDefault(key, "");
    }
}
