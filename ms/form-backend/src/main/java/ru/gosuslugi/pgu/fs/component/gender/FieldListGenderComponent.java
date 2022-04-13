package ru.gosuslugi.pgu.fs.component.gender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.components.descriptor.FieldGroup;
import ru.gosuslugi.pgu.components.descriptor.attr_factory.FieldComponentAttrsFactory;
import ru.gosuslugi.pgu.components.dto.FormDto;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.component.AbstractGenderComponent;

import java.util.List;
import java.util.Objects;

import static ru.gosuslugi.pgu.components.ComponentAttributes.HIDDEN_EMPTY_FIELDS;
import static ru.gosuslugi.pgu.components.ComponentAttributes.HIDDEN_EMPTY_GROUPS;
import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.FieldList;
import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.GFieldList;

@Slf4j
@Component
@RequiredArgsConstructor
public class FieldListGenderComponent extends AbstractGenderComponent<FormDto> {

    @Override
    public void processGender(FieldComponent component, ScenarioDto scenarioDto) {
        super.processGender(component, scenarioDto);
        String gender = Objects.nonNull(userPersonalData.getUserId()) && Objects.nonNull(userPersonalData.getPerson()) ? userPersonalData.getPerson().getGender() : null;
        FieldComponentAttrsFactory attrsFactory = new FieldComponentAttrsFactory(component);
        boolean hiddenEmptyGroups = attrsFactory.getBooleanAttr(HIDDEN_EMPTY_GROUPS, true);
        boolean hiddenEmptyFields = attrsFactory.getBooleanAttr(HIDDEN_EMPTY_FIELDS, false);
        List<FieldGroup> fieldGroups = attrsFactory.getComponentFieldGroups();
        fieldGroups.stream()
                .flatMap(fieldGroup -> fieldGroup.getFields().stream())
                .forEach(field -> {
                    field.setLabel(getLabel(gender, field));
                    field.setValue(getValue(gender, field));
                });

        component.getAttrs().put("fieldGroups", attrsFactory.getComponentFieldGroupsMap(fieldGroups, hiddenEmptyGroups, hiddenEmptyFields));
    }

    @Override
    public ComponentType getType() {
        return GFieldList;
    }

    @Override
    protected ComponentType getTargetComponentType() {
        return FieldList;
    }
}
