package ru.gosuslugi.pgu.common.certificate.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.certificate.dto.BarBarBokRequestDto;
import ru.gosuslugi.pgu.common.certificate.dto.BarBarBokResponseDto;
import ru.gosuslugi.pgu.common.certificate.dto.CertificateCancelResponseDto;
import ru.gosuslugi.pgu.common.certificate.dto.CertificateDataBalanceDto;
import ru.gosuslugi.pgu.common.certificate.dto.CertificateDataResponseDto;
import ru.gosuslugi.pgu.common.certificate.service.mapper.UniversalCertificateRequestMapper;
import ru.gosuslugi.pgu.common.certificate.service.marshaller.CertEaisdoXmlMarshaller;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.CertificateData;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.ProgramRegister;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.UniversalCertificateRequest;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.UniversalCertificateResponse;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.service.ExternalService;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Сервис для запроса данных о сертификате ДО
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateEaisdoService implements ExternalService {
    private static final String ORDER_ID_TAG = "orderId";
    private final UniversalCertificateRequestMapper converter = Mappers.getMapper(UniversalCertificateRequestMapper.class);
    private final CertEaisdoXmlMarshaller marshaller;
    private final BarbarbokClient searcher;

    @Value("${barbarbok.smevVersion:SMEV30MESSAGE}")
    @Getter
    @Setter
    private String smevVersion;

    @Override
    public String getServiceCode() {
        return "certificateEaisdoRequest";
    }

    @Override
    public String sendRequest(FieldComponent certificateEaisdoComponent) {
        Map<String, String> componentArguments = certificateEaisdoComponent.getArguments();
        String orderId = (String) certificateEaisdoComponent.getAttrs().getOrDefault(ORDER_ID_TAG, componentArguments.getOrDefault(ORDER_ID_TAG, "0"));
        BarBarBokRequestDto dto = makeCertificateRequest(componentArguments, orderId);
        BarBarBokResponseDto responseDto = searcher.get(dto);
        String errorMessage = responseDto.getErrorMessage();
        if (StringUtils.hasText(errorMessage)) {
            return JsonProcessingUtil.toJson(Map.of("status", responseDto.getStatus(), "message", errorMessage));
        }
        return calcResult(responseDto);
    }

    private AtomicReference<CertificateDataResponseDto> getInitialResponse(UniversalCertificateResponse certificateResponse) {
        AtomicReference<CertificateDataResponseDto> response = new AtomicReference<>(CertificateDataResponseDto.builder().build());
        if (0 == certificateResponse.getOrderId()) {
            response.set(response.get().toBuilder().isTimeout(true).build());
        }
        return response;
    }

    private String getDefaultTimeout(Map<String, String> componentArguments) {
        return componentArguments.getOrDefault("timeout", "20");
    }

    public BarBarBokRequestDto makeCertificateRequest(Map<String, String> componentArguments, String orderId) {
        UniversalCertificateRequest request = converter.convert(componentArguments, orderId);
        String xmlString = marshaller.marshal(request);
        BarBarBokRequestDto dto = BarBarBokRequestDto.builder()
                .timeout(Long.parseLong(getDefaultTimeout(componentArguments)))
                .data(xmlString)
                .ttl(10L)
                .smevVersion(smevVersion)
                .build();

        return dto;
    }

    private CertificateDataResponseDto mapFromCancel(UniversalCertificateResponse.CertificateResponse response, CertificateDataResponseDto dto) {
        Long cancelCode = Long.parseLong(response.getCancelResponse().getCancelReasonCode());

        return dto.toBuilder()
                .cancelResponse(new CertificateCancelResponseDto(cancelCode, response.getCancelResponse().getCancelReasonDescription()))
                .build();
    }

    private CertificateDataResponseDto mapFromIneffectual(UniversalCertificateResponse.CertificateResponse response, CertificateDataResponseDto dto) {
        String certificateGUID = response.getIneffectualSearchResponse().getCertificateGUID();
        response.getIneffectualSearchResponse().getEmptySearchResultReason();

        return dto.toBuilder()
                .certificateGUID(certificateGUID)
                .build();
    }

    private CertificateDataResponseDto mapFromCertificateData(UniversalCertificateResponse.CertificateResponse response, CertificateDataResponseDto dto) {
        CertificateData certificateData = response.getCertificateDataResponse().getCertificateData();

        return dto.toBuilder()
                .certificateGUID(certificateData.getCertificateGUID())
                .certificateType(Long.parseLong(certificateData.getCertificateType()))
                .certificateNumber(certificateData.getCertificateNumber())
                .certificateCategoryName(certificateData.getCertificateCategory().getCertificateCategoryName())
                .certificateCategoryCode(Long.parseLong(certificateData.getCertificateCategory().getCertificateCategoryCode()))
                .pfdo(getSafeBalanceData(certificateData.getRegisterOfPFAE()))
                .preprof(getSafeBalanceData(certificateData.getRegisterOfPreProfessionalPrograms()))
                .valued(getSafeBalanceData(certificateData.getRegisterOfSignificantPrograms()))
                .other(getSafeBalanceData(certificateData.getRegisterOfOtherPrograms()))
                .build();
    }

    private CertificateDataBalanceDto getSafeBalanceData(ProgramRegister register) {
        return Optional.ofNullable(register)
                .map(it -> new CertificateDataBalanceDto(it.getCertificateBalance(), it.getCertificateBookedAmount()))
                .orElse(new CertificateDataBalanceDto(0, 0));
    }

    public String calcResult(BarBarBokResponseDto responseDto) {
        if ("DONE".equals(responseDto.getStatus())) {
            UniversalCertificateResponse certificateResponse = marshaller.unmarshal(responseDto.getData());

            AtomicReference<CertificateDataResponseDto> response = getInitialResponse(certificateResponse);
            response.set(
                    certificateResponse.getCertificateResponses().stream()
                            .filter(it -> it.getCancelResponse() != null)
                            .findFirst()
                            .map(it -> mapFromCancel(it, response.get())).orElse(response.get())
            );

            response.set(
                    certificateResponse.getCertificateResponses().stream()
                            .filter(it -> it.getIneffectualSearchResponse() != null)
                            .findFirst()
                            .map(it -> mapFromIneffectual(it, response.get()))
                            .orElse(response.get())
            );
            response.set(
                    certificateResponse.getCertificateResponses().stream()
                            .filter(it -> it.getCertificateDataResponse() != null)
                            .findFirst()
                            .map(it -> mapFromCertificateData(it, response.get()))
                            .orElse(response.get())
            );
            return JsonProcessingUtil.toJson(response.get());
        }

        return JsonProcessingUtil.toJson(getInitialResponse(new UniversalCertificateResponse()));

    }
}
