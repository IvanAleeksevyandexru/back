package ru.gosuslugi.pgu.fs.pgu.dto.medicalBirthCertificate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class XmlLink {
    private String objectId;
    private String objectTypeId;
    private String mnemonic;
}
