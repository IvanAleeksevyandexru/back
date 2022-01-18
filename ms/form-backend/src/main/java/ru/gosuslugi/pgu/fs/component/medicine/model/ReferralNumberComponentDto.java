package ru.gosuslugi.pgu.fs.component.medicine.model;

import lombok.Data;

@Data
public class ReferralNumberComponentDto {
    private MedDictionaryResponseCode statusCode;
    private Integer errorCode;
    private String errorMessage;
    private ReferralDto referral;
}
