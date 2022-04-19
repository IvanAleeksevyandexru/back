package ru.gosuslugi.pgu.common.certificate.converters;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import ru.gosuslugi.pgu.common.certificate.service.mapper.ApplicantDataMapper;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.ApplicantData;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

@Slf4j
public class ApplicantDataConverterTest {

    @Test
    public void convertTest() {

        FieldComponent component = makeComponent();
        log.info("{}", "===========================================");
        ApplicantData applicantData = ApplicantDataMapper.INSTANCE.convert(component.getArguments());
        log.info("LastName:{}", applicantData.getApplicantLastName());
        log.info("FirstName:{}", applicantData.getApplicantFirstName());
        log.info("MiddleName:{}", applicantData.getApplicantPatronymic());
        log.info("birthDate:{}", applicantData.getApplicantBirthDate());
        log.info("email:{}", applicantData.getApplicantEmail());
        log.info("phone:{}", applicantData.getApplicantPhoneNumber());
        log.info("address {}", applicantData.getApplicantRegAddress().getFullAddress());
        log.info("passport date {}", applicantData.getApplicantPassportData().getPassportIssueDate());
        log.info("passport series {}", applicantData.getApplicantPassportData().getPassportSeries());
        log.info("passport number {}", applicantData.getApplicantPassportData().getPassportNumber());
        log.info("passport issued {}", applicantData.getApplicantPassportData().getPassportAgency());
        log.info("passport issued code {}", applicantData.getApplicantPassportData().getPassportDepartmentCode());
        log.info("passport type code {}", applicantData.getApplicantPassportData().getPassportType().getCode());

        Assert.assertEquals("72cc79b7-c0d5-4a7f-9934-aca2b2d2d2ab", applicantData.getApplicantRegAddress().getFiasCode());
        Assert.assertEquals("0", applicantData.getApplicantPassportData().getPassportType().getCode());
    }

    private FieldComponent makeComponent() {
        ComponentCertEaisdoFixture fixture = new ComponentCertEaisdoFixture();
        return fixture.makeApplicantComponent();
    }

}

