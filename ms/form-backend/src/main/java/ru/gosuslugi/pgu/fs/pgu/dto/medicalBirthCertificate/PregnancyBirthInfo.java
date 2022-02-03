package ru.gosuslugi.pgu.fs.pgu.dto.medicalBirthCertificate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PregnancyBirthInfo {
    private String firstAppearance;
    private String childbirthTook;
    private String childbirthPlace;
    private String childbirthPlaceCode;
    private String childbirthInfo;
    private String childrenBorn;
}
