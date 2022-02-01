package ru.gosuslugi.pgu.fs.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class InitBookingRequestDto {

    @Schema(description = "Id родительского заявления")
    private Long parentOrderId;

    @Schema(description = "Id услуги")
    private String serviceId;
}
