package ru.gosuslugi.pgu.fs.pgu.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import ru.gosuslugi.pgu.fs.pgu.dto.EmpowermentDTOV2.Power;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmpowermentListDTOV2 {
    private List<Power> elements;
}
