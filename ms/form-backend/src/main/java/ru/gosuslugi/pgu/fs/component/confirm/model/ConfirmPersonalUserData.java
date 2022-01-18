package ru.gosuslugi.pgu.fs.component.confirm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfirmPersonalUserData {

    private String firstName;
    private String lastName;
    private String middleName;
    private String birthDate;
    private String birthPlace;
    private String gender;
    private String genderFull;
    private String docType;
    private String foreignPasportSeries;
    private String foreignPasportNumber;
    private String foreignPasportIssueDate;
    private String foreignPasportIssuedBy;
    private String rfPasportSeries;
    private String rfPasportNumber;
    private String rfPasportIssueDate;
    private String rfPasportIssuedBy;
    private String rfPasportIssuedById;
    private String rfPasportIssuedByIdFormatted;
    private String frgnPasportSeries;
    private String frgnPasportNumber;
    private String frgnPasportIssueDate;
    private String frgnPasportIssuedBy;
    private String frgnPasportExpiryDate;
    private String frgnPasportLastName;
    private String frgnPasportFirstName;
    private String citizenship;
    private String citizenshipCode;
    private String snils;
    private String inn;
    private String omsSeries;
    private String omsNumber;

}
