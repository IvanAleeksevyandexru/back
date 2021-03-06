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
import ru.gosuslugi.pgu.fs.service.ConfirmEmailService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static ru.gosuslugi.pgu.components.ComponentAttributes.VERIFIED_ATTR;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfirmEmailCodeInputComponent extends AbstractComponent<String> {

    private final ConfirmEmailService confirmEmailService;
    private final UserPersonalData userPersonalData;
    private final ErrorModalDescriptorService errorModalDescriptorService;

    @Override
    public ComponentType getType() {
        return ComponentType.ConfirmEmailCodeInput;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        if (scenarioDto.getCachedAnswers().containsKey("internalProcessSuccess") ||
                scenarioDto.getApplicantAnswers().containsKey("internalProcessSuccess")) {
            return ComponentResponse.empty();
        }

        String email = Optional.ofNullable(userPersonalData.getContacts())
                .flatMap(list -> list.stream()
                        .filter(c -> Objects.equals(c.getType(), EsiaContact.Type.EMAIL.getCode())
                                && Objects.equals(c.getVrfStu(), VERIFIED_ATTR))
                        .findAny())
                .map(EsiaContact::getValue)
                .orElse(null);
        component.getAttrs().put("email", email);
        Long originalOrderId = Long.parseLong(scenarioDto.getServiceInfo().getRoutingCode());
        HttpStatus requestStatus = confirmEmailService.sendConfirmationEmail(originalOrderId);

        if (requestStatus.is2xxSuccessful()) return ComponentResponse.empty();

        if (HttpStatus.TOO_MANY_REQUESTS.equals(requestStatus)) {
            throw new ErrorModalException(
                    errorModalDescriptorService.getErrorModal(ErrorModalView.TOO_MANY_REQUESTS_EMAIL),
                    "?????????????????????? ???????????? ?????????????????? ?????????? ?????????????????????????? ???? 5 ????????, ???????? ???????????????????? ???????????????? ???????? ???????????????? 10 ??????. ???????????????? ?????? ???????????????? ?????????? ?????????? 15 ??????????"
            );
        }

        throw new FormBaseWorkflowException("???????????? ?????????????????? ?? ??????????????");
    }

    @Override
    public List<ValidationRule> getValidations() {
        return List.of(new NotBlankValidation("?????????????? email"));
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        String confirmCode = AnswerUtil.getValue(entry);
        Long originalOrderId = Long.parseLong(scenarioDto.getServiceInfo().getRoutingCode());
        HttpStatus requestStatus = confirmEmailService.checkConfirmationEmail(confirmCode, originalOrderId);

        if (requestStatus.is2xxSuccessful()) return;

        if (HttpStatus.CONFLICT.equals(requestStatus)) {
            incorrectAnswers.put(entry.getKey(), "???????????????????????? ??????");
            return;
        }

        if (HttpStatus.TOO_MANY_REQUESTS.equals(requestStatus)) {
            incorrectAnswers.put(entry.getKey(), "?????????????????? ???????????????????? ?????????????? ?????????? ????????");
            return;
        }

        if (HttpStatus.REQUEST_TIMEOUT.equals(requestStatus)) {
            incorrectAnswers.put(entry.getKey(), "?????????? ???????? ???????????????? ????????");
            return;
        }

        if (requestStatus.is5xxServerError() || requestStatus.is4xxClientError()) {
            incorrectAnswers.put(entry.getKey(), "???????????? ?????????????????? ?? ??????????????");
        }
    }
}
