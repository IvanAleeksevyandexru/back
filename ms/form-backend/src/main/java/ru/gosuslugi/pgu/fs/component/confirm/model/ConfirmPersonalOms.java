package ru.gosuslugi.pgu.fs.component.confirm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfirmPersonalOms {
    private String series;
    private String number;
    private String unitedNumber;
    private String issuePlace;
    private String issuedBy;
    private String medicalOrg;
}
