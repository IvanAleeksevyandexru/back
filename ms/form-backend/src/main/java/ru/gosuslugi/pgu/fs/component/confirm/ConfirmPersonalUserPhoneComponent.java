package ru.gosuslugi.pgu.fs.component.confirm;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.atc.carcass.security.rest.model.EsiaContact;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static ru.gosuslugi.pgu.components.ComponentAttributes.VERIFIED_ATTR;


/**
 * Компонент показывает телефон пользователя из ЕСИА и валидирует что он не изменен
 */
@Component
@RequiredArgsConstructor
public class ConfirmPersonalUserPhoneComponent extends AbstractComponent<String> {

    private final UserPersonalData userPersonalData;

    @Override
    public ComponentType getType() {
        return ComponentType.ConfirmPersonalUserPhone;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        String phone = Optional.ofNullable(userPersonalData.getContacts())
                .flatMap(list -> list.stream()
                        .filter(c -> (Objects.equals(c.getType(), EsiaContact.Type.MOBILE_PHONE.getCode()))
                                && Objects.equals(c.getVrfStu(), VERIFIED_ATTR))
                        .findAny())
                .map(EsiaContact::getValue)
                .orElse(null);

        if (!StringUtils.hasText(phone)) {
            FieldComponentUtil.fillComponentErrorFromHint(component);
            return ComponentResponse.empty();
        }
        return ComponentResponse.of(phone);
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, String key, String phone) {
        if (!validate(phone)) {
            incorrectAnswers.put(key, "Phone number not verified in ESIA");
        }
    }

    private boolean validate(String value) {
        return Optional.ofNullable(userPersonalData.getContacts())
                .flatMap(list -> list.stream()
                        .filter(c -> (Objects.equals(c.getType(), EsiaContact.Type.MOBILE_PHONE.getCode()))
                                && Objects.equals(c.getVrfStu(), VERIFIED_ATTR))
                        .filter(c -> Objects.equals(c.getValue(), value))
                        .findAny())
                .isPresent();
    }

}
