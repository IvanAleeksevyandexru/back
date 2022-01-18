package ru.gosuslugi.pgu.fs.service.timer.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.PguTimer;
import ru.gosuslugi.pgu.common.core.exception.EntityNotFoundException;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.fs.delirium.model.DeliriumStageDto;
import ru.gosuslugi.pgu.fs.service.DeliriumService;
import ru.gosuslugi.pgu.fs.service.TimerClient;
import ru.gosuslugi.pgu.fs.service.TimerService;
import ru.gosuslugi.pgu.fs.service.timer.model.TimerRequestParameters;

import java.util.Objects;


@RequiredArgsConstructor
@Service
@Slf4j
public class TimerServiceImpl implements TimerService {

    private final DeliriumService deliriumService;

    private final TimerClient timerClient;

    private final UserPersonalData userPersonalData;

    @Override
    public PguTimer getTimer(Long orderId, String timerCode) {
        DeliriumStageDto stage = deliriumService.getStage(orderId);
        if (Objects.isNull(stage.getTimerToken())) {
            throw new FormBaseException("Failed to get timer from timer service, provided timer uuid is null");
        }

        try {
            TimerRequestParameters parameters = new TimerRequestParameters(
                    stage.getTimerToken(),
                    stage.getTimerObjectId(),
                    timerCode
            );
            return timerClient.getTimer(parameters, userPersonalData.getToken());
        } catch (EntityNotFoundException e) {
            log.warn("Unable to find timer with token {} and objectId {}", stage.getTimerToken(), stage.getTimerObjectId());
            return null;
        }
    }
}
