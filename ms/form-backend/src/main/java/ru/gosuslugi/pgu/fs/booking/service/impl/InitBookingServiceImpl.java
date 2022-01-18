package ru.gosuslugi.pgu.fs.booking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.fs.booking.dto.InitBookingRequestDto;
import ru.gosuslugi.pgu.fs.booking.dto.InitBookingResponseDto;
import ru.gosuslugi.pgu.fs.booking.process.InitBookingProcess;
import ru.gosuslugi.pgu.fs.booking.service.InitBookingService;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitBookingServiceImpl implements InitBookingService {

    private final InitBookingProcess initBookingProcess;

    @Override
    public InitBookingResponseDto getInitBookingScreen(InitBookingRequestDto initBookingDto) {
        return initBookingProcess
                .of(initBookingDto)
                .receiveParentOrder()
                .clearFinishedAndCurrentScreens()
                .calculateInitScreen()
                .finish();
    }
}
