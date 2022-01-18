package ru.gosuslugi.pgu.common.certificate.dto;

import lombok.Data;

@Data
public class PersonStoreValuesDto {
    private String firstName;
    private String middleName;
    private String lastName;
    private String birthDate;
    private String gender;
    private String snils;
}
