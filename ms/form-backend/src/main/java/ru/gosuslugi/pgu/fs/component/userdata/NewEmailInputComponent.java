package ru.gosuslugi.pgu.fs.component.userdata;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.validation.MaxLengthValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.RegExpValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.RequiredNotBlankValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;
import ru.gosuslugi.pgu.fs.service.OrgContactService;
import ru.gosuslugi.pgu.fs.service.PersonContactService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class NewEmailInputComponent extends AbstractComponent<String> {

    private final PersonContactService personContactService;
    private final OrgContactService orgContactService;
    private final UserPersonalData userPersonalData;

    @Override
    public ComponentType getType() {
        return ComponentType.NewEmailInput;
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
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, String key, String value) {
        if (personContactService.isEmailIsAlreadyUsed(value)) {
            incorrectAnswers.put(key, "Адрес электронной почты уже используется в другой учётной записи");
        }
    }

    @Override
    protected void postProcess(FieldComponent component, ScenarioDto scenarioDto, String value) {
        if (Objects.isNull(userPersonalData.getOrgId())) {
            personContactService.updateUserEmail(scenarioDto, value);
        } else {
            orgContactService.updateEmail(scenarioDto, value);
        }
    }
}
