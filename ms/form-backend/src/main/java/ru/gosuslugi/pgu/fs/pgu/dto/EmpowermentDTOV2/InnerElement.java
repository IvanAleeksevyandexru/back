package ru.gosuslugi.pgu.fs.pgu.dto.EmpowermentDTOV2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InnerElement {
    private String mnemonic;
}