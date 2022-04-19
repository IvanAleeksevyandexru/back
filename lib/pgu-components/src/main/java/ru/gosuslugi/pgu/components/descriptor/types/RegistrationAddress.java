package ru.gosuslugi.pgu.components.descriptor.types;

import lombok.Data;

@Data
public class RegistrationAddress {
    private FullAddress regAddr;
    private String regDate;
    private String regZipCode;
    private String fias;
}
