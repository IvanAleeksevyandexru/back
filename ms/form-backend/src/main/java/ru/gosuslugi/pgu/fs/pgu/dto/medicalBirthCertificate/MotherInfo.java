package ru.gosuslugi.pgu.fs.pgu.dto.medicalBirthCertificate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MotherInfo {
    private String fullName;
    private String birthDate;
    private String registrationArea;
    private String registrationAreaCode;
    private String familyStatus;
    private String familyStatusCode;
    private String education;
    private String employment;
}
