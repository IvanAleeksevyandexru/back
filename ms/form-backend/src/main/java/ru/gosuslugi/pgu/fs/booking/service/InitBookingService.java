package ru.gosuslugi.pgu.fs.booking.service;

import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.fs.booking.dto.InitBookingRequestDto;
import ru.gosuslugi.pgu.fs.booking.dto.InitBookingResponseDto;

public interface InitBookingService {

    InitBookingResponseDto getInitBookingScreen(InitBookingRequestDto initBookingDto);

}
