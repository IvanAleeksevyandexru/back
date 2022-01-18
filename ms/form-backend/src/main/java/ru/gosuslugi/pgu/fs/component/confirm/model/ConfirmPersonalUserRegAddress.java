package ru.gosuslugi.pgu.fs.component.confirm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfirmPersonalUserRegAddress {

    private String regAddr;
    private String regZipCode;
    private String fias;
    private String regDate;

}
