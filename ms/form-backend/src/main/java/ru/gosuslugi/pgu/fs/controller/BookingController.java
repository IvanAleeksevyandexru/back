package ru.gosuslugi.pgu.fs.controller;

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
    public InitBookingResponseDto initBooking(@RequestBody InitBookingRequestDto initBookingDto){
        return bookingService.getInitBookingScreen(initBookingDto);
    }

}
