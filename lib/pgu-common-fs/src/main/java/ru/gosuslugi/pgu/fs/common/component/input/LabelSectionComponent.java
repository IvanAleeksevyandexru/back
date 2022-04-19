package ru.gosuslugi.pgu.fs.common.component.input;

import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.components.BasicComponentUtil;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractCycledComponent;

import java.util.Map;
import java.util.Set;

@Component
public class LabelSectionComponent extends AbstractCycledComponent<String> {

    @Override
    public ComponentType getType() {
        return ComponentType.LabelSection;
    }

    @Override
    protected void preProcessCycledComponent(FieldComponent component, Map<String, Object> externalData) {
        Set<String> fieldNames = BasicComponentUtil.getPreSetFields(component);
        fieldNames.forEach(key ->
                externalData.computeIfPresent(key, (k, v) ->{
                    component.setLabel(component.getLabel().concat(" ").concat(v.toString()).trim());
                    return v;
                })
        );
    }
}
