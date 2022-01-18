package ru.gosuslugi.pgu.fs.booking.service;

import ru.gosuslugi.pgu.dto.ScenarioDto;

public interface BookingService {

    void sendBookingInfo(String serviceId, ScenarioDto scenarioDto);

}
