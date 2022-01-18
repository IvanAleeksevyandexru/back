package ru.gosuslugi.pgu.fs.booking.dto;

import lombok.Data;

@Data
public class InitBookingRequestDto {

    private Long parentOrderId;

    private String serviceId;
}
