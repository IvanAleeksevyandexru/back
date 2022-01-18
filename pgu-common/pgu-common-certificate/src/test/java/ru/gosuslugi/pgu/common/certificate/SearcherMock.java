package ru.gosuslugi.pgu.common.certificate;

import lombok.extern.slf4j.Slf4j;
import ru.gosuslugi.pgu.common.certificate.dto.BarBarBokRequestDto;
import ru.gosuslugi.pgu.common.certificate.dto.BarBarBokResponseDto;
import ru.gosuslugi.pgu.common.certificate.service.BarbarbokClient;
import ru.gosuslugi.pgu.common.certificate.service.marshaller.CertEaisdoXmlMarshaller;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.*;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;

@Slf4j
public class SearcherMock implements BarbarbokClient {
    private String responseXml = ResourceFileReader.readFromFile("responsexml.xml");
    String responseString = ResourceFileReader.readFromFile("responsestring.json");

    @Override
    public BarBarBokResponseDto get(BarBarBokRequestDto dto, int timeout) {
        CertEaisdoXmlMarshaller unmarshaller = new CertEaisdoXmlMarshaller();
        BarBarBokResponseDto response = JsonProcessingUtil.fromJson(responseString, BarBarBokResponseDto.class) ;
        UniversalCertificateResponse result = unmarshaller.unmarshal(response.getData());
        result.getCertificateResponses().forEach(it -> {
                    log.info("--------------{}", it.getCertificateDataResponse().getCertificateData().getCertificateCategory().getCertificateCategoryCode());
                    log.info("--------------{}", it.getCertificateDataResponse().getCertificateData().getCertificateCategory().getCertificateCategoryName());
                    log.info("--------------{}", it.getCertificateDataResponse().getCertificateData().getRegisterOfPFAE().getCertificateBalance());
                    log.info("--------------{}", it.getCertificateDataResponse().getCertificateData().getRegisterOfPFAE().getCertificateBookedAmount());

                }
        );

        return response;
    }

    @Override
    public BarBarBokResponseDto getUnsafe(BarBarBokRequestDto dto, int timeout) {
        return get(dto, -1);
    }

    @Override
    public BarBarBokResponseDto get(BarBarBokRequestDto dto) {
        return get(dto, -1);
    }

    public String getSmevVersion() {
        return "SMEV30MESSAGE";
    }

    private CertificateDataResponse getMockCertificateDataResponse() {
        CertificateDataResponse response = new CertificateDataResponse();
        CertificateData data = new CertificateData();
        data.setCertificateGUID("d0a79bf5-ee86-4224-9b22-a1f300ba0e83");
        data.setCertificateNumber("987654321");
        ProgramRegister register = new ProgramRegister();
        register.setCertificateBookedAmount(1500.0D);
        register.setCertificateBalance(20000.0D);
        data.setRegisterOfPreProfessionalPrograms(register);

        CertificateData.CertificateCategory category = new CertificateData.CertificateCategory();
        category.setCertificateCategoryCode("4");
        category.setCertificateCategoryName("Категория сертификата");
        data.setCertificateCategory(category);
        data.setCertificateType("0");
        response.setCertificateData(data);
        CertificateOwnerData ownerData = new CertificateOwnerData();
        ownerData.setRecipientSNILS("1234567890");
        ownerData.setRecipientLastName("Шмокодявкин");
        ownerData.setRecipientFirstName("Кузьма");
        ownerData.setRecipientPatronymic("Антихович");
        response.setCertificateOwnerData(ownerData);

        return response;
    }

    private IneffectualSearchResponse getMockIneffectualSearchResponse() {
        IneffectualSearchResponse response = new IneffectualSearchResponse();
        response.setCertificateGUID("d0a79bf5-ee86-4224-9b22-a1f300ba0e83");
        response.setEmptySearchResultReason("Сертификат не найден");
        response.setRecipientSNILS("1234567890");
        return response;
    }

    private CancelResponse getMockCancelResponse() {
        CancelResponse cancelResponse = new CancelResponse();
        cancelResponse.setCancelReasonCode("404");
        cancelResponse.setCancelReasonDescription("Сертификат не найден");
        return cancelResponse;
    }
}
