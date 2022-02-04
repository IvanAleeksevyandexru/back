package ru.gosuslugi.pgu.fs.pgu.dto.medicalBirthCertificate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentInfo {
    private String series;
    private String number;
    private String issueDate;
}
