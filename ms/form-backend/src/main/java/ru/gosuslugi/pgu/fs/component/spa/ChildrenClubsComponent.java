package ru.gosuslugi.pgu.fs.component.spa;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.component.validation.PredicateValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;

import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class ChildrenClubsComponent extends AbstractComponent<String> {

    private final PredicateValidation predicateValidation;

    @Override
    public ComponentType getType() {
        return ComponentType.ChildrenClubs;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component) {
        String value = jsonProcessingService.toJson(Map.of("state", component.getArguments()));
        return ComponentResponse.of(value);
    }

    @Override
    public List<ValidationRule> getValidations() {
        return List.of(predicateValidation);
    }
}
