package ru.gosuslugi.pgu.fs.component.child.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChildData {

    private String firstName;
    private String middleName;
    private String lastName;
    private String birthDate;
    private String gender;
    private String snils;

    private String omsNumber;
    private String omsSeries;

    private String rfBirthCertificateSeries;
    private String rfBirthCertificateNumber;
    private String rfBirthCertificateActNumber;
    private String rfBirthCertificateIssueDate;
    private String rfBirthCertificateIssuedBy;
    private String actDate;

    private String docType;
}
