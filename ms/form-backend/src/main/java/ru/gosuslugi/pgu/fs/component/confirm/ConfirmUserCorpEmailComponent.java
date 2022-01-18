package ru.gosuslugi.pgu.fs.component.confirm;

import org.springframework.stereotype.Component;
import ru.atc.carcass.security.rest.model.person.EsiaRole;
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

import static ru.gosuslugi.pgu.fs.component.confirm.ConfirmLegalDataComponent.USER_TYPE_ERROR;

/**
 * Компонент показывает email пользователя из ЕСИА и валидирует что он не изменен
 * Электронная почта сотрудника в организации
 */
@Component
public class ConfirmUserCorpEmailComponent extends ConfirmPersonalUserEmailComponent {

    public ConfirmUserCorpEmailComponent(UserPersonalData userPersonalData, UserOrgData userOrgData) {
        super(userPersonalData, userOrgData);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.ConfirmUserCorpEmail;
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
        return  Optional.ofNullable(userOrgData.getOrgRole())
            .map(EsiaRole::getEmail);
    }

    @Override
    protected boolean isPresentAndVerifiedEmail(String value) {
        return getEmail()
            .filter(email -> email.equals(value))
            .isPresent();
    }
}
