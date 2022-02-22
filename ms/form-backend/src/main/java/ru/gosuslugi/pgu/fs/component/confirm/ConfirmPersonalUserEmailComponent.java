package ru.gosuslugi.pgu.fs.component.confirm;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.atc.carcass.security.rest.model.EsiaContact;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.components.RegExpUtil;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponentError;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static ru.gosuslugi.pgu.components.ComponentAttributes.ENABLE_CUSTOM_VALIDATION_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ERROR_DESC_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ERROR_MSG_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.VALUE_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.VERIFIED_ATTR;

/**
 * Компонент показывает email пользователя из ЕСИА и валидирует что он не изменен
 */
@Component
@AllArgsConstructor
public class ConfirmPersonalUserEmailComponent extends AbstractComponent<String> {

    public static final String DEFAULT_EMAIL_MASK = "^[0-9а-яА-Яa-zA-Z_.-]{1,50}[@]{1}[0-9а-яА-Яa-zA-Z_.-]{2,50}[.]{1}[а-яА-Яa-zA-Z]{2,10}$";
    public static final String DEFAULT_EMAIL_ERROR_MSG = "Введите корректный адрес электронной почты";
    public static final String DEFAULT_EMAIL_ERROR_DESC = "";
    public static final String EMPTY_EMAIL_WARRING_MSG = "Не обнаружен подтвержденный электронный адрес для пользователя";
    public static final String EMPTY_EMAIL_WARRING_DESC = "Подтвердите переходом по ссылке из письма с подтверждением или добавьте новый адрес электронной почты через форму с помощью кнопки \"Редактировать\"";
    public static final String NOT_VERIFIED_EMAIL_ERROR_MSG = "Электронная почта не подтверждена в профиле ЕСИА";
    public static final Map<String, String> DEFAULT_REG_EXP_VALIDATION_PARAMS = Map.of(
            VALUE_ATTR, DEFAULT_EMAIL_MASK,
            ERROR_MSG_ATTR, DEFAULT_EMAIL_ERROR_MSG,
            ERROR_DESC_ATTR, DEFAULT_EMAIL_ERROR_DESC);

    protected final UserPersonalData userPersonalData;
    protected final UserOrgData userOrgData;

    @Override
    public ComponentType getType() {
        return ComponentType.ConfirmPersonalUserEmail;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        return getEmail().map(ComponentResponse::of).orElseGet(ComponentResponse::empty);
    }

    @Override
    protected void preValidate(ComponentResponse<String> initialValue, FieldComponent component, ScenarioDto scenarioDto) {
        String email = initialValue.get();
        List<FieldComponentError> errors = new ArrayList<>();
        if (StringUtils.isEmpty(email)) {
            FieldComponentUtil.fillComponentErrorFromHint(component);
        } else {
            List<Map<String, String>> regExpValidations = getRegExpValidationAttrs(component);
            for (Map<String, String> regExpValidation: regExpValidations) {
                if (!email.matches(regExpValidation.get(VALUE_ATTR))) {
                    errors.add(new FieldComponentError(regExpValidation.get(ERROR_MSG_ATTR), regExpValidation.get(ERROR_DESC_ATTR)));
                }
            }
        }

        if (!CollectionUtils.isEmpty(errors)) {
            component.setErrors(errors);
        }
    }

    @Override
    public void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent component) {
        String email = entry.getValue().getValue();
        // проверяем на заполнение
        if (StringUtils.isEmpty(email)) {
            incorrectAnswers.put(entry.getKey(), EMPTY_EMAIL_WARRING_MSG);
            return;
        }
        // проверяем по регуляркам
        List<Map<String, String>> regExpValidations = getRegExpValidationAttrs(component);
        for (Map<String, String> regExpValidation: regExpValidations) {
            if (!email.matches(regExpValidation.get(VALUE_ATTR))) {
                incorrectAnswers.put(entry.getKey(), regExpValidation.get(ERROR_MSG_ATTR));
                return;
            }
        }
        // проверяем подтверждение электронной почты
        if (!isPresentAndVerifiedEmail(email)) {
            incorrectAnswers.put(entry.getKey(), NOT_VERIFIED_EMAIL_ERROR_MSG);
        }
    }

    protected Optional<String> getEmail() {
        return Optional.ofNullable(userPersonalData.getContacts())
                .flatMap(list -> list.stream()
                        .filter(c -> (Objects.equals(c.getType(), EsiaContact.Type.EMAIL.getCode())
                                && Objects.equals(c.getVrfStu(), VERIFIED_ATTR)))
                        .findAny())
                .map(EsiaContact::getValue);
    }

    protected boolean isPresentAndVerifiedEmail(String value) {
        return Optional.ofNullable(userPersonalData.getContacts())
                .flatMap(list -> list.stream()
                        .filter(c -> (Objects.equals(c.getType(), EsiaContact.Type.EMAIL.getCode())
                                && Objects.equals(c.getVrfStu(), VERIFIED_ATTR)))
                        .filter(c -> value.equalsIgnoreCase(c.getValue()))
                        .findAny())
                .isPresent();
    }

    protected List<Map<String, String>> getRegExpValidationAttrs(FieldComponent component) {
        List<Map<String, String>> result = null;
        String booleanStringValue = String.valueOf(component.getAttrs().getOrDefault(ENABLE_CUSTOM_VALIDATION_ATTR, "false"));
        boolean isCustomValidation = Boolean.parseBoolean(booleanStringValue);
        if (isCustomValidation) {
            List<Map<String, String>> regExpValidations = RegExpUtil.getValidationRegExpList(component);
            if (!CollectionUtils.isEmpty(regExpValidations)) {
                result = regExpValidations;
            }
        }

        return result != null ? result : List.of(DEFAULT_REG_EXP_VALIDATION_PARAMS);
    }
}
