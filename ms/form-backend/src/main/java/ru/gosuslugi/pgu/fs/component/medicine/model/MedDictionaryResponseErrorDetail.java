package ru.gosuslugi.pgu.fs.component.medicine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class MedDictionaryResponseErrorDetail {
    private Integer errorCode;
    private String errorCodeTxt;
    private String errorMessage;
}
