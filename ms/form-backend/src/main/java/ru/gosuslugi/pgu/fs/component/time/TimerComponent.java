package ru.gosuslugi.pgu.fs.component.time;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.PguTimer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.service.TimerService;

import java.util.Objects;

import static java.util.Objects.nonNull;
import static ru.gosuslugi.pgu.components.ComponentAttributes.*;

/**
 *
 * Данный класс подтягивает поля для компонента подтверждения бронирования с таймером,
 * Найденные данные записываются в поле FieldComponent.value в JSON-формате в виде Map<String, Object>.
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimerComponent extends AbstractComponent<String> {

    @NonNull
    private final TimerService timerService;

    @Override
    public ComponentType getType() {
        return ComponentType.Timer;
    }

    @Override
    protected void preProcess(FieldComponent component, ScenarioDto scenarioDto) {
        if (component != null && scenarioDto != null && component.getAttrs() != null) {
            if (Objects.nonNull(component.getAttrs().get(TIMER_CODE_ATTR))) {
                // идём в сервис таймеров и достаём таймер из него
                PguTimer timer = timerService.getTimer(scenarioDto.getOrderId(), component.getAttrs().get(TIMER_CODE_ATTR).toString());
                if (nonNull(timer)) {
                    component.getAttrs().put(START_TIME_ATTR, timer.getStartTime());
                    component.getAttrs().put(EXPIRATION_TIME_ATTR, timer.getExpirationTime());
                    component.getAttrs().put(CURRENT_TIME_ATTR, timer.getCurrentTime());
                }
                return;
            }
            // время начала и конца таймера должны быть проставлены в json через refs
            componentReferenceService.processComponentRefs(component, scenarioDto);
        }
    }

}
