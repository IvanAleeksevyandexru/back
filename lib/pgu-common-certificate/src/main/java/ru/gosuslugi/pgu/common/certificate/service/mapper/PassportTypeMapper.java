package ru.gosuslugi.pgu.common.certificate.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.PassportDataType;

import java.util.Map;

@Mapper
public abstract class PassportTypeMapper {
    public static final PassportTypeMapper INSTANCE = Mappers.getMapper(PassportTypeMapper.class);

    @Mapping(target = "code", constant = "0")
    @Mapping(target = "value", constant = "Паспорт РФ")
    public abstract PassportDataType.PassportType convert(Map<String, String> componentArguments);

}
