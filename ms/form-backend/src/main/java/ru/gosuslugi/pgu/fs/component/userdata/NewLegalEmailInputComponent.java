package ru.gosuslugi.pgu.fs.component.userdata;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.validation.MaxLengthValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.RegExpValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.RequiredNotBlankValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;
import ru.gosuslugi.pgu.fs.service.OrgContactService;

import java.util.Arrays;
import java.util.List;

/**
 * Компонент добавления/изменение email-а организации
 * Внутренний сценарий
 */
@Component
@RequiredArgsConstructor
public class NewLegalEmailInputComponent extends AbstractComponent<String> {

    private final OrgContactService orgContactService;

    @Override
    public ComponentType getType() {
        return ComponentType.NewLegalEmailInput;
    }

    @Override
    public List<ValidationRule> getValidations() {
        return Arrays.asList(
                new RequiredNotBlankValidation("Введите адрес электронной почты"),
                new RegExpValidation(),
                new MaxLengthValidation()
        );
    }

    @Override
    protected void postProcess(FieldComponent component, ScenarioDto scenarioDto, String value) {
        orgContactService.updateEmail(scenarioDto, value);
    }
}
