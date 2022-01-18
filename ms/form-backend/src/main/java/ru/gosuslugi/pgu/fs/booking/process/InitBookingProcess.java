package ru.gosuslugi.pgu.fs.booking.process;

import ru.gosuslugi.pgu.fs.booking.dto.InitBookingRequestDto;
import ru.gosuslugi.pgu.fs.booking.dto.InitBookingResponseDto;

public interface InitBookingProcess {

    InitBookingProcess of(InitBookingRequestDto initBookingRequestDto);

    InitBookingProcess clearFinishedAndCurrentScreens();

    InitBookingProcess receiveParentOrder();

    InitBookingProcess calculateInitScreen();

    InitBookingResponseDto finish();

}
