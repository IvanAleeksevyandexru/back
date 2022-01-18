package ru.gosuslugi.pgu.fs.component.medicine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class MedDictionaryResponse {
    private MedDictionaryResponseError error;
    Integer totalItems;
    List<MedDictionaryResponseItem> items;
}
