package ru.gosuslugi.pgu.fs.component.userdata.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MedicalSimpleItem {
    private String attributeName;
    private String condition;
    private String value;
    private Boolean checkAllValues;
}
