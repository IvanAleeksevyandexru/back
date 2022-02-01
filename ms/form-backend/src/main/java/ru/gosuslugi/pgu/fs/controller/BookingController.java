package ru.gosuslugi.pgu.fs.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gosuslugi.pgu.fs.booking.dto.InitBookingResponseDto;
import ru.gosuslugi.pgu.fs.booking.dto.InitBookingRequestDto;
import ru.gosuslugi.pgu.fs.booking.service.InitBookingService;

@RestController
@RequestMapping(value = "service/booking", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class BookingController {

    private final InitBookingService bookingService;

    @PostMapping
    @Operation(summary = "Инициализация формы записи на прием для перехода к ней со страницы деталей заявления")
    public InitBookingResponseDto initBooking(@RequestBody InitBookingRequestDto initBookingDto){
        return bookingService.getInitBookingScreen(initBookingDto);
    }

}
