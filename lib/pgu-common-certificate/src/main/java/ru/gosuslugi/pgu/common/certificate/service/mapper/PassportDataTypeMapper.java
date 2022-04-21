package ru.gosuslugi.pgu.common.certificate.service.mapper;

import lombok.SneakyThrows;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.gosuslugi.pgu.common.certificate.dto.PersonCertEaisdoDto;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.PassportDataType;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.components.dto.FieldDto;
import ru.gosuslugi.pgu.components.dto.StateDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(imports = JsonProcessingUtil.class)
public abstract class PassportDataTypeMapper {

    public static final PassportDataTypeMapper INSTANCE = Mappers.getMapper(PassportDataTypeMapper.class);
    protected final String PASSPORT = "Паспорт гражданина РФ";

    protected List<FieldDto> passportFieldDtos;
    protected String passportIssueDate;

    @SneakyThrows
    @BeforeMapping
    protected void init(Map<String, String> componentArguments) {
        String applicantJson = componentArguments.getOrDefault(ApplicantDataMapper.ApplicantFields.applicantData.value, "");
        var applicantPerson = JsonProcessingUtil.fromJson(applicantJson, PersonCertEaisdoDto.class);
        passportFieldDtos = applicantPerson.getStates().stream()
                .filter(it -> PASSPORT.equals(it.getGroupName()))
                .map(StateDto::getFields)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        passportIssueDate = CalendarConverter.formatDate(calcDocumentData(passportFieldDtos, DocumentFields.DATE));
    }

    @Mapping(target = "passportAgency", expression = "java(calcDocumentData(passportFieldDtos, DocumentFields.ISSUER))")
    @Mapping(target = "passportIssueDate", expression = "java(passportIssueDate)")
    @Mapping(target = "passportDepartmentCode", expression = "java(calcDocumentData(passportFieldDtos, DocumentFields.CODE))")
    @Mapping(target = "passportSeries", expression = "java(calcDocNumber(passportFieldDtos).getSeries())")
    @Mapping(target = "passportNumber", expression = "java(calcDocNumber(passportFieldDtos).getNumber())")
    @Mapping(target = "passportType", expression = "java(PassportTypeMapper.INSTANCE.convert(componentArguments))")
    public abstract PassportDataType convert(Map<String, String> componentArguments);


    protected SeriesAndNumber calcDocNumber(List<FieldDto> fieldDtos) {
        String series = calcDocumentData(fieldDtos, DocumentFields.SERIES);
        StringBuilder number = new StringBuilder(calcDocumentData(fieldDtos, DocumentFields.NUMBER));
        if (number.length() == 0) {
            String series_and_number = calcDocumentData(fieldDtos, DocumentFields.SERIES_AND_NUMBER);
            String[] split = series_and_number.split("\\s");
            if (split.length == 0) {
                return new SeriesAndNumber(series, series_and_number);
            } else {
                series = split[0];
                for (int i = 1; i < split.length; i++) {
                    number.append(split[i]);
                }
            }
        }
        return new SeriesAndNumber(series, number.toString());
    }

    protected String calcDocumentData(List<FieldDto> fieldDtos, DocumentFields field) {
        return fieldDtos.stream()
                .filter(it -> field.value.equalsIgnoreCase(it.getLabel()))
                .map(FieldDto::getValue)
                .findFirst().orElse("");
    }

    enum DocumentFields {
        SERIES_AND_NUMBER("серия и номер"),
        SERIES("серия"),
        NUMBER("номер"),
        DATE("дата выдачи"),
        ISSUER("кем выдан"),
        CODE("код подразделения");
        private final String value;

        DocumentFields(String value) {
            this.value = value;
        }
    }

}
