package ru.gosuslugi.pgu.fs.common.helper;

import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ScreenType;
import ru.gosuslugi.pgu.dto.ScenarioDto;

public interface ScreenHelper {
    default ScreenDescriptor processScreen(ScreenDescriptor screenDescriptor, ScenarioDto scenarioDto) {
        return screenDescriptor;
    }
    public ScreenType getType();
}
