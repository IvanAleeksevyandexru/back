package ru.gosuslugi.pgu.fs.pgu.mapper;

import org.mapstruct.Mapper;
import ru.gosuslugi.pgu.dto.descriptor.HighloadParameters;
import ru.gosuslugi.pgu.fs.pgu.dto.HighLoadOrderRequestDto;

@Mapper
public interface HighLoadOrderPguMapper {
    HighLoadOrderRequestDto toDto(HighloadParameters highloadParameters);
}
