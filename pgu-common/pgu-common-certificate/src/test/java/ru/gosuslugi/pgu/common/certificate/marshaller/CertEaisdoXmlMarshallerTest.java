package ru.gosuslugi.pgu.common.certificate.marshaller;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import ru.gosuslugi.pgu.common.certificate.converters.ComponentCertEaisdoFixture;
import ru.gosuslugi.pgu.common.certificate.service.mapper.ApplicantDataMapper;
import ru.gosuslugi.pgu.common.certificate.service.mapper.CertificateRecipientDataMapper;
import ru.gosuslugi.pgu.common.certificate.service.mapper.PassportDataTypeMapper;
import ru.gosuslugi.pgu.common.certificate.service.mapper.RegAddressMapper;
import ru.gosuslugi.pgu.common.certificate.service.marshaller.CertEaisdoXmlMarshaller;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.ApplicantData;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.CertificateRecipientData;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.GetCertificateStatus;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.UniversalCertificateRequest;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

@Slf4j
public class CertEaisdoXmlMarshallerTest {
    private final CertEaisdoXmlMarshaller marshaller = new CertEaisdoXmlMarshaller();

    @Test
    public void marshallingTest() {
        PassportDataTypeMapper passportDataTypeConverter = Mappers.getMapper(PassportDataTypeMapper.class);
        RegAddressMapper regAddressConverter = Mappers.getMapper(RegAddressMapper.class);

        FieldComponent component = makeComponent();
        log.info("{}", "===========================================");
        ApplicantDataMapper converter = Mappers.getMapper(ApplicantDataMapper.class);
        ApplicantData applicantData = converter.convert(component.getArguments());
        UniversalCertificateRequest dto = new UniversalCertificateRequest();
        GetCertificateStatus certificateStatus = new GetCertificateStatus();
        certificateStatus.setApplicantData(applicantData);
        dto.setGetCertificateStatusRequest(certificateStatus);
        String marshal = marshaller.marshal(dto);
        log.info("{}", marshal);
        Assert.assertTrue(marshal.contains("<tns:applicantLastName>Николаев</tns:applicantLastName>"));
        Assert.assertTrue(marshal.contains("<tns:street>А.Алибаева</tns:street>"));
//        Assert.assertTrue(marshal.contains("<tns:fiasCode>72cc79b7-c0d5-4a7f-9934-aca2b2d2d2ab</tns:fiasCode>"));
        Assert.assertTrue(marshal.contains("</tns:getCertificateStatusRequest>"));

    }

    @Test
    public void marshallingBirthCertificateTest() {
        FieldComponent component = makeFullComponent();
        log.info("{}", "===========================================");
        CertificateRecipientDataMapper certificateRecipientDataMapper = CertificateRecipientDataMapper.INSTANCE;
        CertificateRecipientData certificateRecipientData = certificateRecipientDataMapper.convert(component.getArguments());
        UniversalCertificateRequest dto = new UniversalCertificateRequest();
        GetCertificateStatus certificateStatus = new GetCertificateStatus();
        certificateStatus.setCertificateRecipientData(certificateRecipientData);
        dto.setGetCertificateStatusRequest(certificateStatus);
        String marshal = marshaller.marshal(dto);
        log.info("{}", marshal);
        Assert.assertTrue(marshal.contains("<tns:recipientBirthCertificateData>"));
        Assert.assertFalse(marshal.contains("<tns:recipientForeignBirthCertificateData/>"));

    }
    @Test
    public void marshallingForeignOnlyBirthCertificateTest() {
        FieldComponent component = makeComponentWithForeignBirthCertificate();
        log.info("{}", "===========================================");
        CertificateRecipientDataMapper certificateRecipientDataMapper = CertificateRecipientDataMapper.INSTANCE;
        CertificateRecipientData certificateRecipientData = certificateRecipientDataMapper.convert(component.getArguments());
        UniversalCertificateRequest dto = new UniversalCertificateRequest();
        GetCertificateStatus certificateStatus = new GetCertificateStatus();
        certificateStatus.setCertificateRecipientData(certificateRecipientData);
        dto.setGetCertificateStatusRequest(certificateStatus);
        String marshal = marshaller.marshal(dto);
        log.info("{}", marshal);
        Assert.assertFalse(marshal.contains("<tns:recipientBirthCertificateData>"));
        Assert.assertTrue(marshal.contains("<tns:recipientForeignBirthCertificateData>"));

    }
        private FieldComponent makeComponent() {
        ComponentCertEaisdoFixture fixture = new ComponentCertEaisdoFixture();
        return fixture.makeApplicantComponent();
    }
        private FieldComponent makeFullComponent() {
        ComponentCertEaisdoFixture fixture = new ComponentCertEaisdoFixture();
        return fixture.makeComponent();
    }
        private FieldComponent makeComponentWithForeignBirthCertificate() {
        ComponentCertEaisdoFixture fixture = new ComponentCertEaisdoFixture();
        return fixture.makeComponentWithForeignBirthCertificate();
    }

}