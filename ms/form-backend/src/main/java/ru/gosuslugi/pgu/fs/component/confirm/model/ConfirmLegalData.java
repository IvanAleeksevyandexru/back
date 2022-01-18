package ru.gosuslugi.pgu.fs.component.confirm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfirmLegalData {
    private String fullName;
    private String shortName;
    private String ogrn;
    private String inn;
    private String kpp;

    private String chiefOid;
    private String chiefFirstName;
    private String chiefLastName;
    private String chiefMiddleName;
    private String chiefBirthDate;
    private String chiefPosition;
}
