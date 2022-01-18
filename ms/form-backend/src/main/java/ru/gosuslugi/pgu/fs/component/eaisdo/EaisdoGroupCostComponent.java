package ru.gosuslugi.pgu.fs.component.eaisdo;


import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;

@Component
public class EaisdoGroupCostComponent extends AbstractComponent<String> {

    @Override
    public ComponentType getType() {
        return ComponentType.EaisdoGroupCost;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        return ComponentResponse.of("valid");
    }
}
