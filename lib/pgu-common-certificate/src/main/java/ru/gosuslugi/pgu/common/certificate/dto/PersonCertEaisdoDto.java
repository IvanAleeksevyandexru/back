package ru.gosuslugi.pgu.common.certificate.dto;

import lombok.Data;
import ru.gosuslugi.pgu.components.dto.StateDto;

import java.util.List;

@Data
public class PersonCertEaisdoDto {
    private List<StateDto> states;
    private PersonStoreValuesDto storedValues;
    private String phone;
    private String email;
}
