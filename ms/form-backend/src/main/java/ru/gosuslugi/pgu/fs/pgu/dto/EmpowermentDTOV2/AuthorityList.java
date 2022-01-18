package ru.gosuslugi.pgu.fs.pgu.dto.EmpowermentDTOV2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorityList {
    private List<InnerElement> elements;
}