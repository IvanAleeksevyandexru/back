package ru.gosuslugi.pgu.fs.component.gender;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.component.AbstractGenderComponent;

@Component
@RequiredArgsConstructor
public class StringComponentGenderComponent extends AbstractGenderComponent<String> {

    @Override
    public ComponentType getType() {
        return ComponentType.GStringInput;
    }

    @Override
    protected ComponentType getTargetComponentType() {
        return ComponentType.StringInput;
    }
}
