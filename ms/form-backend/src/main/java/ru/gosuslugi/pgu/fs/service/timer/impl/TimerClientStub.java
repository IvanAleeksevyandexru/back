package ru.gosuslugi.pgu.fs.service.timer.impl;

import ru.gosuslugi.pgu.dto.PguTimer;
import ru.gosuslugi.pgu.fs.service.TimerClient;
import ru.gosuslugi.pgu.fs.service.timer.model.TimerRequestParameters;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

public class TimerClientStub implements TimerClient {

    /** Начальное время таймера, либо now (сейчас), либо строка ISO-8601 */
    public static String START_TIME = "now";

    /** Продолжительность таймера в секундах */
    public static int DURATION = 300;

    @Override
    public PguTimer getTimer(TimerRequestParameters parameters, String authToken) {
        String timerCode = parameters.getCode();

        PguTimer timer = new PguTimer();

        OffsetDateTime startTime = getStartTime();
        timer.setStartTime(startTime.toString());

        OffsetDateTime expirationTime = startTime.plusSeconds(DURATION);
        timer.setExpirationTime(expirationTime.toString());

        timer.setUnit(TimeUnit.SECONDS);
        timer.setDuration(DURATION);
        timer.setCode(timerCode);

        timer.setActive(expirationTime.toEpochSecond() > ZonedDateTime.now().toEpochSecond());
        return timer;
    }

    private OffsetDateTime getStartTime() {
        return "now".equalsIgnoreCase(START_TIME) ? OffsetDateTime.now() : OffsetDateTime.parse(START_TIME);
    }
}
