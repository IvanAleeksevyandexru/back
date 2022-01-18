package ru.gosuslugi.pgu.fs.component.confirm;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.component.validation.NotBlankValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;
import ru.gosuslugi.pgu.fs.service.OrgContactService;

import java.util.List;
import java.util.Map;

/**
 * Компонент подтверждения электронной почты организации
 * Внутренний сценарий
 */
@Component
@RequiredArgsConstructor
public class ConfirmNewLegalEmailComponent extends AbstractComponent<String> {

    private final OrgContactService orgContactService;

    private static final String EMAIL_ATTR = "email";

    @Override
    public ComponentType getType() {
        return ComponentType.ConfirmLegalNewEmail;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component) {
        return ComponentResponse.of(
                component.getArgument(EMAIL_ATTR)
        );
    }

    @Override
    public List<ValidationRule> getValidations() {
        return List.of(
            new NotBlankValidation("Введите адрес электронной почты")
        );
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, String key, String email) {
        if (!orgContactService.isEmailConfirmedAndLinked(email)) {
            incorrectAnswers.put(key, "Адрес электроной почты не подтвержден пользователем");
        }
    }
}
