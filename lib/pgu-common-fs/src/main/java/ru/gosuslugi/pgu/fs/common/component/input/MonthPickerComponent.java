package ru.gosuslugi.pgu.fs.common.component.input;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;

@Slf4j
@Component
public class MonthPickerComponent extends EsiaDataCycledComponent<String> {

    @Override
    public ComponentType getType() {
        return ComponentType.MonthPicker;
    }
}
