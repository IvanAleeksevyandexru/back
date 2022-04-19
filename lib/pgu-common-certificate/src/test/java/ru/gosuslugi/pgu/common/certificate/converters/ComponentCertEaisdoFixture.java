package ru.gosuslugi.pgu.common.certificate.converters;

import ru.gosuslugi.pgu.common.certificate.ResourceFileReader;
import ru.gosuslugi.pgu.common.certificate.service.mapper.CalendarConverter;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

import java.util.HashMap;
import java.util.Map;

public class ComponentCertEaisdoFixture {

    public FieldComponent makeComponent() {
        FieldComponent component = new FieldComponent();
        Map<String, String> arguments = new HashMap<>();
        arguments.put("orderId", "987654321");

        arguments.put("LastName", "Шмокодявкин");
        arguments.put("FirstName", "Кузьма");
        arguments.put("MiddleName", "Антихович");
        arguments.put("BirthDate", "2005-09-01");
        arguments.put("BirthCertificateIssueDate", "2005-11-01");
        arguments.put("GenderType", "M");
        arguments.put("BirthCertificateSeries", "XI-ФФ");
        arguments.put("BirthCertificateNumber", "123321");
        arguments.put("BirthCertificateAgency", "Тест");
        arguments.put("SNILS", "");
        arguments.put("isForeign", "false");

        String recipientRegAddress = ResourceFileReader.readFromFile("resipientaddr.json");
        arguments.put("recipientRegAddress", recipientRegAddress);
        arguments.put("pd4.value", recipientRegAddress);

        String applicantRegAddress = ResourceFileReader.readFromFile("applicantregaddress.json");
        arguments.put("applicantRegAddress", applicantRegAddress);


        String applicantData = ResourceFileReader.readFromFile("applicantdata.json");
        arguments.put("applicantData", applicantData);
        arguments.put("pd1.value", applicantData);
        arguments.put("applicantPhone", "\"+7(999)8388383\"");
        arguments.put("pd2.value", "\"+7(999)8388383\"");
        arguments.put("applicantEmail", "\"dgvozdev@smart-consulting.ru\"");
        arguments.put("pd3.value", "\"dgvozdev@smart-consulting.ru\"");
        arguments.put("timeout", "30");

        arguments.put("payment", "pfdod_certificate");
        component.setArguments(arguments);
        return component;
    }

    public FieldComponent makeApplicantComponent() {
        FieldComponent component = new FieldComponent();
        Map<String, String> arguments = new HashMap<>();
        String applicantData = ResourceFileReader.readFromFile("applicantdata.json");
        arguments.put("applicantData", applicantData);
        arguments.put("pd1.value", applicantData);

        arguments.put("pd2.value", "\"+7(999)8388383\"");
        arguments.put("applicantPhone", "\"+7(999)8388383\"");
        arguments.put("pd3.value", "\"rtlabs.test1123@gmail.com\"");
        arguments.put("applicantEmail", "\"rtlabs.test1123@gmail.com\"");
        String applicantRegAddress = ResourceFileReader.readFromFile("applicantregaddress.json");
        arguments.put("applicantRegAddress", applicantRegAddress);
        arguments.put("pd4.value", applicantRegAddress);

        component.setArguments(arguments);
        return component;
    }
    public FieldComponent makeComponentWithForeignBirthCertificate() {
        FieldComponent fieldComponent = makeComponent();
        Map<String, String> arguments = fieldComponent.getArguments();
        arguments.put("isForeign", "true");
        arguments.put("RecipientForeignBirthCertificateNumber", "999999");
        arguments.put("RecipientForeignBirthCertificateAgency", "Some foreign registry");
        arguments.put("RecipientForeignBirthCertificateIssueDate", "01.11.2005");
        arguments.put("RecipientForeignBirthCertificateSeries", "22ZZ");
        return fieldComponent;
    }

}

