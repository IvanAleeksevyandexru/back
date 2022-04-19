package ru.gosuslugi.pgu.fs.common.variable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.fs.common.service.UserCookiesService;

import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Глобальная переменная для вычисления текущего дня и времени
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TodayVariable extends AbstractVariable {

    private final UserCookiesService userCookiesService;

    @Override
    public String getValue(ScenarioDto scenarioDto) {
        return (OffsetDateTime.now()).atZoneSameInstant(ZoneId.of(userCookiesService.getUserTimezone())).toOffsetDateTime().toString();
    }

    @Override
    public VariableType getType() {
        return VariableType.today;
    }
}
