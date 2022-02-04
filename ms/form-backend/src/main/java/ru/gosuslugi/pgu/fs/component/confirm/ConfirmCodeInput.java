package ru.gosuslugi.pgu.fs.component.confirm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.atc.carcass.security.rest.model.EsiaContact;
import ru.gosuslugi.pgu.fs.common.exception.ErrorModalException;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.component.validation.NotBlankValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalDescriptorService;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalView;
import ru.gosuslugi.pgu.fs.service.ConfirmCodeService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static ru.gosuslugi.pgu.components.ComponentAttributes.VERIFIED_ATTR;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfirmCodeInput extends AbstractComponent<String> {

    private final ConfirmCodeService confirmCodeService;
    private final UserPersonalData userPersonalData;
    private final ErrorModalDescriptorService errorModalDescriptorService;

    @Override
    public ComponentType getType() {
        return ComponentType.ConfirmCodeInput;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {

        if (scenarioDto.getCachedAnswers().containsKey("internalProcessSuccess") ||
                scenarioDto.getApplicantAnswers().containsKey("internalProcessSuccess")) {
            return ComponentResponse.empty();
        }

        String phone = Optional.ofNullable(userPersonalData.getContacts())
                .flatMap(list -> list.stream()
                        .filter(c -> Objects.equals(c.getType(), EsiaContact.Type.MOBILE_PHONE.getCode())
                                && Objects.equals(c.getVrfStu(), VERIFIED_ATTR))
                        .findAny())
                .map(EsiaContact::getValue)
                .orElse(null);
        component.getAttrs().put("phoneNumber", phone);
        Long originalOrderId = Long.parseLong(scenarioDto.getServiceInfo().getRoutingCode());
        HttpStatus requestStatus = confirmCodeService.sendConfirmationCode(originalOrderId);

        if (requestStatus.is2xxSuccessful()) return ComponentResponse.empty();

        if (HttpStatus.TOO_MANY_REQUESTS.equals(requestStatus)) {
            throw new ErrorModalException(
                    errorModalDescriptorService.getErrorModal(ErrorModalView.TOO_MANY_REQUESTS_PHONE),
                    "Ваш номер телефона заблокируется на 5 дней, если количество попыток запроса СМС-кода превысит 10 раз. Получить код повторно можно через 15 минут"
            );
        }

        throw new FormBaseWorkflowException("Ошибка обращения к сервису");
    }

    @Override
    public List<ValidationRule> getValidations() {
        return List.of(new NotBlankValidation("Введите код подтверждения"));
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        String confirmCode = AnswerUtil.getValue(entry);
        Long originalOrderId = Long.parseLong(scenarioDto.getServiceInfo().getRoutingCode());
        HttpStatus requestStatus = confirmCodeService.checkConfirmationCode(confirmCode, originalOrderId);

        if (requestStatus.is2xxSuccessful()) return;

        if (HttpStatus.CONFLICT.equals(requestStatus)) {
            incorrectAnswers.put(entry.getKey(), "Неправильный код");
            return;
        }

        if (HttpStatus.TOO_MANY_REQUESTS.equals(requestStatus)) {
            incorrectAnswers.put(entry.getKey(), "Превышено количество попыток ввода кода");
            return;
        }

        if (HttpStatus.REQUEST_TIMEOUT.equals(requestStatus)) {
            incorrectAnswers.put(entry.getKey(), "Истек срок действия кода");
            return;
        }

        if (requestStatus.is5xxServerError() || requestStatus.is4xxClientError()) {
            incorrectAnswers.put(entry.getKey(), "Ошибка обращения к сервису");
        }
    }
}
