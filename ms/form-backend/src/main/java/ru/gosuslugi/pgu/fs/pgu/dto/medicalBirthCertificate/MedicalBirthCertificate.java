package ru.gosuslugi.pgu.fs.pgu.dto.medicalBirthCertificate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для получения свидетельства о рождении
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicalBirthCertificate {
    private String oid;
    private String id;
    private String version;
    private String createdOn;
    private String updatedOn;
    private String receiptDocDate;
    private String relevance;
    private String status;
    private String lastName;
    private String firstName;
    private String birthPlace;
    private String gender;
    private String birthDate;
    private String departmentDoc;
    private String medicalOrg;
    private String medicalOrgAddress;
    private String type;
    @JsonProperty("nbInfo")
    private NewBornInfo newBornInfo;
    @JsonProperty("docInfo")
    private DocumentInfo documentInfo;
    @JsonProperty("labodeli")
    private PregnancyBirthInfo pregnancyBirthInfo;
    @JsonProperty("mothInfo")
    private MotherInfo motherInfo;
    private SignatureLink signatureLink;
    private XmlLink xmlLink;
}