package ru.gosuslugi.pgu.fs.component.confirm;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;

import java.util.Map;
import java.util.Objects;

import static java.util.Objects.nonNull;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ORG_PHONE_TYPE_ATTR;
import static ru.gosuslugi.pgu.fs.component.confirm.ConfirmLegalDataComponent.USER_TYPE_ERROR;

/**
 * Компонент показывает телефон организации из ЕСИА и валидирует что он не изменен
 */
@Component
@RequiredArgsConstructor
public class ConfirmLegalPhoneComponent extends AbstractComponent<String> {

    private static final String EMPTY_PHONE_WARRING = "Не обнаружен подтвержденный телефонный номер для организации. Добавьте новый номер через личный кабинет с помощью кнопки \"Редактировать\"";

    private final UserOrgData userOrgData;

    @Override
    public ComponentType getType() {
        return ComponentType.ConfirmLegalPhone;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        if (Objects.isNull(userOrgData.getOrg())) {
            throw new FormBaseWorkflowException(USER_TYPE_ERROR);
        }
        return ComponentResponse.of(userOrgData.getVerifiedContactValue(ORG_PHONE_TYPE_ATTR));
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
        return nonNull(userOrgData.getVerifiedContactValue(ORG_PHONE_TYPE_ATTR));
    }
}
