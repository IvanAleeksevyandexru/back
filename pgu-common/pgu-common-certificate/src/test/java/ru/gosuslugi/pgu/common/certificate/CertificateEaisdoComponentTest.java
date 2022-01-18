package ru.gosuslugi.pgu.common.certificate;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.Assert;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import ru.gosuslugi.pgu.common.certificate.converters.ComponentCertEaisdoFixture;
import ru.gosuslugi.pgu.common.certificate.dto.BarBarBokResponseDto;
import ru.gosuslugi.pgu.common.certificate.dto.CertificateDataResponseDto;
import ru.gosuslugi.pgu.common.certificate.service.CertificateEaisdoService;
import ru.gosuslugi.pgu.common.certificate.service.BarbarbokClient;
import ru.gosuslugi.pgu.common.certificate.service.mapper.*;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.common.certificate.dto.BarBarBokRequestDto;
import ru.gosuslugi.pgu.common.certificate.service.marshaller.CertEaisdoXmlMarshaller;

@Slf4j
public class CertificateEaisdoComponentTest {
    PassportDataTypeMapper passportDataTypeMapper = Mappers.getMapper(PassportDataTypeMapper.class);
    RegAddressMapper regAddressMapper = Mappers.getMapper(RegAddressMapper.class);
    ApplicantDataMapper applicantDataMapper = Mappers.getMapper(ApplicantDataMapper.class);
    ApplicationDetailsMapper applicationDetailsMapper = Mappers.getMapper(ApplicationDetailsMapper.class);

    ForeignBirthCertificateDataTypeMapper foreignBirthCertificateDataTypeMapper =
            Mappers.getMapper(ForeignBirthCertificateDataTypeMapper.class);
    BirthCertificateDataTypeMapper birthCertificateDataTypeMapper = Mappers.getMapper(BirthCertificateDataTypeMapper.class);
    CertificateRecipientDataMapper certificateRecipientDataMapper = Mappers.getMapper(CertificateRecipientDataMapper.class);

    GetCertificateStatusMapper getCertificateStatusMapper = Mappers.getMapper(GetCertificateStatusMapper.class);
    GetCertificateInformerMapper getCertificateInformerMapper = Mappers.getMapper(GetCertificateInformerMapper.class);
    UniversalCertificateRequestMapper converter = Mappers.getMapper(UniversalCertificateRequestMapper.class);
    CertEaisdoXmlMarshaller marshaller = new CertEaisdoXmlMarshaller();

    BarbarbokClient searcher = new SearcherMock();
    private final RestTemplate restTemplate;

    {
        restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    public void makeCertificateRequestTest(){
        FieldComponent fieldComponent = new ComponentCertEaisdoFixture().makeComponent();
        ScenarioDto scenarioDto = new ScenarioDto();
        scenarioDto.setOrderId(763973850L);
        fieldComponent.addArgument("orderId", scenarioDto.getOrderId().toString());
        CertificateEaisdoService service = new CertificateEaisdoService(marshaller, searcher);

        BarBarBokRequestDto barBarBokRequestDto = service.makeCertificateRequest(fieldComponent.getArguments(), fieldComponent.getArgument("orderId"));
        log.info("{}", barBarBokRequestDto.getData());
        log.info("{}", barBarBokRequestDto.getSmevVersion());
        log.info("{}", barBarBokRequestDto.getTimeout());


    }

    @Test
    public void calcResultTest() {
        ScenarioDto scenarioDto = new ScenarioDto();
        scenarioDto.setOrderId(763973850L);
        BarBarBokRequestDto dto = BarBarBokRequestDto.builder()
                .ttl(10L)
                .timeout(30L)
                .build();

        BarBarBokResponseDto search = searcher.get(dto);
        log.info("====================={}",  search);

        CertificateEaisdoService service = new CertificateEaisdoService(marshaller, searcher);
        String s = service.calcResult(search);
        log.info(s);
        CertificateDataResponseDto responseDto = JsonProcessingUtil.fromJson(s, CertificateDataResponseDto.class);
        log.info("{}", responseDto);
        log.info("===================================");
       Assert.assertEquals("d0a79bf5-ee86-4224-9b22-a1f300ba0e83", responseDto.getCertificateGUID());
        Assert.assertEquals(10553.14D, responseDto.getPfdo().getCertificateBalance(), 0.0001);
        Assert.assertEquals(553.14D, responseDto.getPfdo().getCertificateBookedAmount(), 0.0001);
        Assert.assertEquals(10000.0D, responseDto.getPfdo().getCertificateAvailableBalance(), 0.0001);

    }
}