package ru.gosuslugi.pgu.fs.component.confirm;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.atc.carcass.security.rest.model.person.EsiaRole;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static ru.gosuslugi.pgu.fs.component.confirm.ConfirmLegalDataComponent.USER_TYPE_ERROR;

/**
 * Компонент показывает телефон организации из ЕСИА и валидирует что он не изменен
 */
@Component
@RequiredArgsConstructor
public class ConfirmUserCorpPhoneComponent extends AbstractComponent<String> {

    private static final String EMPTY_PHONE_WARRING = "Не обнаружен подтвержденный телефонный номер для организации. Добавьте новый номер через личный кабинет с помощью кнопки \"Редактировать\"";

    private final UserOrgData userOrgData;

    @Override
    public ComponentType getType() {
        return ComponentType.ConfirmUserCorpPhone;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        if (Objects.isNull(userOrgData.getOrg())) {
            throw new FormBaseWorkflowException(USER_TYPE_ERROR);
        }
        String phone = Optional.ofNullable(userOrgData.getOrgRole())
                .map(EsiaRole::getPhone)
                .orElse(null);

        return ComponentResponse.of(phone);
    }

    @Override
    protected void preValidate(ComponentResponse<String> initialValue, FieldComponent component, ScenarioDto scenarioDto) {
        if (StringUtils.isEmpty(initialValue.get())) {
            scenarioDto.getErrors().put(component.getId(), EMPTY_PHONE_WARRING);
        }
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, String key, String phone) {
        if (!validate(phone)) {
            incorrectAnswers.put(key, EMPTY_PHONE_WARRING);
        }
    }

    private boolean validate(String value) {
        return Optional.ofNullable(userOrgData.getOrgRole())
                .map(EsiaRole::getPhone)
                .filter(phone -> phone.equals(value))
                .isPresent();
    }
}
