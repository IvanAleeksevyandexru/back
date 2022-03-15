package ru.gosuslugi.pgu.fs.common.component.userdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;

/**
 * Класс содержит вспомогательные методы компонента ОГРНИП
 * - в последующем компонент должен быть отнаследован от StringInput
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OgrnipComponent extends OgrnComponent {

    private static final String DEFAULT_ERROR_MESSAGE = "Некорректный ОГРНИП";

    @Override
    public ComponentType getType() {
        return ComponentType.OgrnipInput;
    }

    @Override
    public boolean isOgrnip() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getErrorText() {
        return DEFAULT_ERROR_MESSAGE;
    }
}
