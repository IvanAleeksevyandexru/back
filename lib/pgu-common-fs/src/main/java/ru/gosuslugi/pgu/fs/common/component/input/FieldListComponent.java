package ru.gosuslugi.pgu.fs.common.component.input;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.components.dto.FormDto;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.service.ComponentReferenceService;

@Component
@RequiredArgsConstructor
public class FieldListComponent extends AbstractComponent<FormDto> {

    private final ComponentReferenceService componentReferenceService;

    @Override
    public ComponentType getType() {
        return ComponentType.FieldList;
    }

    @Override
    public ComponentResponse<FormDto> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        return ComponentResponse.of(componentReferenceService.processFieldGroups(component, scenarioDto).build());
    }
}
