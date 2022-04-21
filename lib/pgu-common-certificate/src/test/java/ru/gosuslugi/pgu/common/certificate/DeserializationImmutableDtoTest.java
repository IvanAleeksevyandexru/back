package ru.gosuslugi.pgu.common.certificate;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import ru.gosuslugi.pgu.common.certificate.dto.BarBarBokRequestDto;
import ru.gosuslugi.pgu.common.certificate.dto.CertificateCancelResponseDto;
import ru.gosuslugi.pgu.common.certificate.dto.CertificateDataBalanceDto;
import ru.gosuslugi.pgu.common.certificate.dto.CertificateDataResponseDto;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;

@Slf4j
public class DeserializationImmutableDtoTest {

    @Test
    public void serializeToJson(){
        BarBarBokRequestDto dto = BarBarBokRequestDto.builder()
                .data("expected String")
                .ttl(10L)
                .timeout(20L)
                .smevVersion("SMEV30MESSAGE")
                .build();

        String json = JsonProcessingUtil.toJson(dto);
        log.info("{}", json);
        Assert.assertTrue(json.contains("expected String"));
    }

    @Test
    public void deserializeFromJson(){
        String jsonString = "{\"data\":\"expected String\",\"ttl\":10,\"timeout\":20,\"smevVersion\":\"SMEV30MESSAGE\"}";
        BarBarBokRequestDto dto = JsonProcessingUtil.fromJson(jsonString, BarBarBokRequestDto.class);
        log.info("{}", dto);
        Assert.assertEquals("expected String", dto.getData());

    }

    @Test
    public void serializeCertificateDataBalanceDto(){
        CertificateDataBalanceDto dto = new CertificateDataBalanceDto(15.2D, 3.1D);
        String json = JsonProcessingUtil.toJson(dto);
        log.info("{}", json);
        Assert.assertEquals(15.2D, dto.getCertificateBalance(), 0.0001);
        Assert.assertEquals(3.1D, dto.getCertificateBookedAmount(), 0.0001);
        Assert.assertEquals(12.1D, dto.getCertificateAvailableBalance(), 0.0001);
    }

    @Test
    public void deserializeCertificateDataBalanceDto(){
        String jsonString = "{\"certificateBalance\":15.2,\"certificateBookedAmount\":3.1,\"certificateAvailableBalance\":12.1}";
        CertificateDataBalanceDto dto = JsonProcessingUtil.fromJson(jsonString, CertificateDataBalanceDto.class);
        log.info("{}", dto);
        Assert.assertEquals(15.2D, dto.getCertificateBalance(), 0.0001);
        Assert.assertEquals(3.1D, dto.getCertificateBookedAmount(), 0.0001);
        Assert.assertEquals(12.1D, dto.getCertificateAvailableBalance(), 0.0001);
    }

    @Test
    public void serializeCertificateDataResponseDto(){
        CertificateDataResponseDto dto = CertificateDataResponseDto.builder()
                .cancelResponse(new CertificateCancelResponseDto(3L, "some cancel response"))
                .certificateCategoryCode(5L)
                .certificateGUID("0000-0000-0000-0000-0000-0000")
                .certificateCategoryName("certificate name")
                .certificateNumber("1234567890")
                .certificateType(1L)
                .isReleasedOnRequestByEPGU(true)
                .isTimeout(false)
                .other(new CertificateDataBalanceDto(3.0D, 2.0D))
                .pfdo(new CertificateDataBalanceDto(15.1, 9.3))
                .preprof(new CertificateDataBalanceDto(4.0D, 1.0D))
                .valued(new CertificateDataBalanceDto(5.0D, 1.0D))
                .build();
        String json = JsonProcessingUtil.toJson(dto);
        log.info("{}", json);
    }
    @Test
    public void deserializeCertificateDataResponseDto(){
        String jsonString = "{\"certificateGUID\":\"0000-0000-0000-0000-0000-0000\"," +
                "\"certificateNumber\":\"1234567890\",\"certificateType\":1,\"certificateCategoryCode\":5," +
                "\"certificateCategoryName\":\"certificate name\"," +
                "\"pfdo\":{\"certificateBalance\":15.1,\"certificateBookedAmount\":9.3,\"certificateAvailableBalance\":5.799999999999999}," +
                "\"valued\":{\"certificateBalance\":5.0,\"certificateBookedAmount\":1.0,\"certificateAvailableBalance\":4.0}," +
                "\"preprof\":{\"certificateBalance\":4.0,\"certificateBookedAmount\":1.0,\"certificateAvailableBalance\":3.0}," +
                "\"other\":{\"certificateBalance\":3.0,\"certificateBookedAmount\":2.0,\"certificateAvailableBalance\":1.0}," +
                "\"cancelResponse\":{\"cancelReasonCode\":3,\"cancelReasonDescription\":\"some cancel response\"}," +
                "\"timeout\":false," +
                "\"releasedOnRequestByEPGU\":true}";
        CertificateDataResponseDto dto = JsonProcessingUtil.fromJson(jsonString, CertificateDataResponseDto.class);
        log.info("{}", dto);
        Assert.assertEquals(15.1D, dto.getPfdo().getCertificateBalance(), 0.0001);
        Assert.assertEquals(9.3D, dto.getPfdo().getCertificateBookedAmount(), 0.0001);
        Assert.assertEquals(5.8D, dto.getPfdo().getCertificateAvailableBalance(), 0.0001);
        Assert.assertEquals("some cancel response", dto.getCancelResponse().getCancelReasonDescription());
    }

}
