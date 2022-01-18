package ru.gosuslugi.pgu.common.certificate;

import lombok.SneakyThrows;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.*;
import ru.gosuslugi.pgu.common.certificate.dto.PersonCertificateEaisdoRequestGenderType;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class CertificateRequestConverterFixture {
    private final static String EUROPEAN_DATE_PATTERN = "dd.MM.yyyy";
    private final static DateTimeFormatter EUROPEAN_DATE_FORMATTER = DateTimeFormatter.ofPattern(EUROPEAN_DATE_PATTERN);

    private final static String ISO_DATE_PATTERN = "yyyy-MM-dd";
    private final static DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern(ISO_DATE_PATTERN);

    public UniversalCertificateRequest getFakeUniversalCertificateRequest() {
        UniversalCertificateRequest dto = new UniversalCertificateRequest();
        dto.setOrderId(0L);
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2021, Calendar.DECEMBER, 17, 9, 30, 47);
        dto.setOrderTimestamp(calendar);
        dto.setGetCertificateStatusRequest(getFakeCertificateStatusRequest());
        return dto;
    }

    private GetCertificateStatus getFakeCertificateStatusRequest() {
        GetCertificateStatus dto = new GetCertificateStatus();
        dto.setCertificateRecipientData(getFakeCertificateRecipientData());
        dto.setApplicantData(getFakeApplicantData());
        dto.setApplicationDetails(getFakeApplicationDetails());
        return dto;
    }

    private ApplicationDetails getFakeApplicationDetails() {
        ApplicationDetails dto = new ApplicationDetails();

        dto.setMunicipalityCode("6b1bab7d-ee45-4168-a2a6-4ce2880d90d3");
        dto.setMunicipalityName("г Ярославль");
        dto.setRegionCode("a84b2ef4-db03-474b-b552-6229e801ae9b");
        dto.setRegionName("Ярославская область");
        dto.setIsFundedCertificate(true);

        return dto;
    }

    @SneakyThrows
    private CertificateRecipientData getFakeCertificateRecipientData() {
        CertificateRecipientData dto = new CertificateRecipientData();
        dto.setRecipientLastName("Иванов");
        dto.setRecipientFirstName("Андрей");
        dto.setRecipientPatronymic("Иванович");



        dto.setRecipientBirthDate(LocalDate.of(2008, 10, 23).format(ISO_DATE_FORMATTER));
        dto.setRecipientSNILS("55677424739");
        dto.setRecipientGenderType(PersonCertificateEaisdoRequestGenderType.Male.name());
        dto.setRecipientBirthCertificateData(getFakeRecipientBirthCertificateData());
        dto.setRecipientRegAddress(getFakeRegAddress());

        return dto;
    }

    private XMLGregorianCalendar getXmlGregorianCalendar(LocalDate dt) throws DatatypeConfigurationException {
        XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(dt.toString());
        return xmlGregorianCalendar;
    }

    @SneakyThrows
    private BirthCertificateDataType getFakeRecipientBirthCertificateData() {
        BirthCertificateDataType dto = new BirthCertificateDataType();
        dto.setRecipientBirthCertificateSeries("AA");
        dto.setRecipientBirthCertificateNumber("123456");
        dto.setRecipientBirthCertificateAgency("Районный ЗАГС");
        dto.setRecipientBirthCertificateIssueDate(LocalDate.of(2008, 10, 30).format(ISO_DATE_FORMATTER));
        return dto;
    }

    @SneakyThrows
    private ApplicantData getFakeApplicantData() {
        ApplicantData dto = new ApplicantData();
        dto.setApplicantFirstName("Иван");
        dto.setApplicantPatronymic("Иванович");
        dto.setApplicantBirthDate(LocalDate.of(1972, 8, 13).format(ISO_DATE_FORMATTER));
        dto.setApplicantPassportData(getFakeApplicantPassportData());
        dto.setApplicantRegAddress(getFakeRegAddress());
        dto.setApplicantEmail("test@mail.ru");
        dto.setApplicantPhoneNumber("79997773535");
        return dto;
    }

    private RegAddress getFakeRegAddress() {
        RegAddress dto = new RegAddress();
        dto.setFullAddress("150000, Ярославская обл., г. Ярославль, ул. Кирова, д. 25, кв. 10");
        dto.setPostIndex("150000");
        dto.setCountry("Россия");
        dto.setRegion("Ярославская область");
        dto.setCity("Ярославль");
        dto.setStreet("Кирова");
        dto.setHouse("25");
        dto.setApartment("10");
        dto.setFiasCode("b2f23b22-bc0b-46b6-a48f-e107fba5b396");

        return dto;
    }

    @SneakyThrows
    private PassportDataType getFakeApplicantPassportData() {
        PassportDataType dto = new PassportDataType();
        PassportDataType.PassportType passportType = new PassportDataType.PassportType();
        passportType.setCode("PASSPORT");
        passportType.setValue("Паспорт РФ");

        dto.setPassportType(passportType);
        dto.setPassportSeries("12 34");
        dto.setPassportNumber("123456");
        dto.setPassportAgency("отделение ОУМФСО по г. Москве");
        dto.setPassportIssueDate(LocalDate.of(2011, 9, 7).format(ISO_DATE_FORMATTER));
        dto.setPassportDepartmentCode("777");
        return dto;
    }

}
