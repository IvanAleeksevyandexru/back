package ru.gosuslugi.pgu.fs.common.variable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ScenarioDto;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Класс переменной, которая возвращает форматированное текущее время для UTC таймзоны в виде строки
 * Пример: 2021-05-08T12:33:44.555Z
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TodayTimeStampVariable extends AbstractVariable {

    /** Тайм зона, для которой будет сгенерено текущее время */
    public static final ZoneOffset ZONE_OFFSET = ZoneOffset.UTC;

    /** Форматер даты для генерации строки текущего времени для указанной таймзоты */
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .withZone(TodayTimeStampVariable.ZONE_OFFSET);

    @Override
    public String getValue(ScenarioDto scenarioDto) {
        return OffsetDateTime.now().format(DATE_TIME_FORMATTER);
    }

    @Override
    public VariableType getType() {
        return VariableType.todayTimeStamp;
    }
}
