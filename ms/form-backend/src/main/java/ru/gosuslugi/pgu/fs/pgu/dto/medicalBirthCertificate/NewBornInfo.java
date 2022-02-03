package ru.gosuslugi.pgu.fs.pgu.dto.medicalBirthCertificate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewBornInfo {
    private String gender;
    private String birthDate;
    private String birthTime;
    private String weight;
    private String height;
    private String genderCode;
    private String birthAreaCode;
}
