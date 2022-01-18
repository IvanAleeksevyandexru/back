package ru.gosuslugi.pgu.fs.component.medicine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MedDictionaryResponseAttribute {
    private String name;
    private String value;
}
