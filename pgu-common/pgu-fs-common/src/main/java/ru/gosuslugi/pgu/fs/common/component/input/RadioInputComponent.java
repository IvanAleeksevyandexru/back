package ru.gosuslugi.pgu.fs.common.component.input;

import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;

@Component
public class RadioInputComponent extends EsiaDataCycledComponent<String> {
    @Override
    public ComponentType getType() {
        return ComponentType.RadioInput;
    }
}
