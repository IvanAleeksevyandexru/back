package ru.gosuslugi.pgu.common.certificate.service.mapper;

import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;

import java.util.Map;

@Mapper
public abstract class RecipientBirthCertificateData {

    protected String agency;
    protected String issueDate;
    protected String series;
    protected String number;

    @BeforeMapping
    protected void init(Map<String, String> componentArguments){
        series = componentArguments.getOrDefault("BirthCertificateSeries", null);
        number = componentArguments.getOrDefault("BirthCertificateNumber", null);
        agency = componentArguments.getOrDefault("BirthCertificateAgency", null);
        String birthCertificateIssueDate = componentArguments.getOrDefault("BirthCertificateIssueDate", null);
        if (birthCertificateIssueDate != null) {
            issueDate = CalendarConverter.formatDate(birthCertificateIssueDate);
        }
    }
}
