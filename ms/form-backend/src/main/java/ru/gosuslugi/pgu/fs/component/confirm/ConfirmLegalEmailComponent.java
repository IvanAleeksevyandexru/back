package ru.gosuslugi.pgu.fs.component.confirm;

import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ORG_EMAIL_TYPE_ATTR;
import static ru.gosuslugi.pgu.fs.component.confirm.ConfirmLegalDataComponent.USER_TYPE_ERROR;

/**
 * Компонент показывает email организации пользователя из ЕСИА и валидирует что он не изменен
 * Электронная почта организации
 */
@Component
public class ConfirmLegalEmailComponent extends ConfirmPersonalUserEmailComponent {

    public ConfirmLegalEmailComponent(UserPersonalData userPersonalData, UserOrgData userOrgData) {
        super(userPersonalData, userOrgData);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.ConfirmLegalEmail;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        if (Objects.isNull(userOrgData.getOrg())) {
            throw new FormBaseWorkflowException(USER_TYPE_ERROR);
        }
        return super.getInitialValue(component, scenarioDto);
    }

    @Override
    public void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent component) {
        super.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, component);
    }

    @Override
    protected Optional<String> getEmail() {
        return Optional.ofNullable(userOrgData.getVerifiedContactValue(ORG_EMAIL_TYPE_ATTR));
    }

    @Override
    protected boolean isPresentAndVerifiedEmail(String value) {
        return nonNull(userOrgData.getVerifiedContactValue(ORG_EMAIL_TYPE_ATTR));
    }
}
