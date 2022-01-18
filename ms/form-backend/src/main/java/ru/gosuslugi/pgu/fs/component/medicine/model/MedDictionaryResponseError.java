package ru.gosuslugi.pgu.fs.component.medicine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class MedDictionaryResponseError {
    private MedDictionaryResponseErrorDetail errorDetail;
}
