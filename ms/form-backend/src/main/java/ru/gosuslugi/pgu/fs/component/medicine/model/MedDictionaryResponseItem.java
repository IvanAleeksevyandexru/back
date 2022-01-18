package ru.gosuslugi.pgu.fs.component.medicine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class MedDictionaryResponseItem {
    List<MedDictionaryResponseAttribute> attributes;
    Map<String,String> convertedAttributes;
}