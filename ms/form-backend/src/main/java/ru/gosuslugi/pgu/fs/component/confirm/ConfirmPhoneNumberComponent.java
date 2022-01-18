package ru.gosuslugi.pgu.fs.component.confirm;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.component.validation.NotBlankValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;
import ru.gosuslugi.pgu.fs.esia.dto.EsiaContactState;
import ru.gosuslugi.pgu.fs.service.PersonContactService;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ConfirmPhoneNumberComponent extends AbstractComponent<String> {

    private static final String WARNING_USED_PHONE_NUMBER = "Указанный номер уже используется в другой учетной записи на портале. Если номер корректный, подтвердите его кодом из отправленного СМС. После подтверждения номер привяжется к вашей текущей учетной записи.";
    private static final String PHONE_NUMBER_ARGUMENT_NAME = "phoneNumber";
    private static final String ERROR_CONFIRM_CODE_MESSAGE = "Код не подходит. Проверьте еще раз";

    private final PersonContactService personContactService;

    @Override
    public ComponentType getType() {
        return ComponentType.PhoneNumberConfirmCodeInput;
    }

    @Override
    protected void preValidate(ComponentResponse<String> initialValue, FieldComponent component, ScenarioDto scenarioDto) {
        String phoneNumber = component.getArgument(PHONE_NUMBER_ARGUMENT_NAME);
        if (StringUtils.isEmpty(phoneNumber)) {
            throw new FormBaseWorkflowException("No phone number provided");
        }
        String phoneNumberForEsia = personContactService.preparePhoneNumberForEsia(phoneNumber);
        EsiaContactState phoneUsedResponse = personContactService.validatePhoneNumber(phoneNumberForEsia);
        if (phoneUsedResponse.isState()) {
            if (Objects.isNull(component.getAttrs())) {
                component.setAttrs(new HashMap<>());
            }
            component.getAttrs().put("validateMessage", WARNING_USED_PHONE_NUMBER);
        }
    }

    @Override
    public List<ValidationRule> getValidations() {
        return List.of(
                new NotBlankValidation("Введите код подтверждения")
        );
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        String confirmCode = AnswerUtil.getValue(entry);
        if (!personContactService.checkConfirmationCode(confirmCode, scenarioDto)) {
            incorrectAnswers.put(entry.getKey(), ERROR_CONFIRM_CODE_MESSAGE);
        }
    }
}
