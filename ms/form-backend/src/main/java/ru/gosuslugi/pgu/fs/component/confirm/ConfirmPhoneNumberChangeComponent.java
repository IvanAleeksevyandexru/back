package ru.gosuslugi.pgu.fs.component.confirm;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.validation.NotBlankValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.RegExpValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;
import ru.gosuslugi.pgu.fs.esia.dto.EsiaContactState;
import ru.gosuslugi.pgu.fs.service.PersonContactService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ConfirmPhoneNumberChangeComponent extends AbstractComponent<String> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String WARNING_PHONE_USED_IN_LAST_30_DAYS = "Номер мобильного телефона уже используется в другой учетной записи. Если вы уже регистрировались, попробуйте войти в свою учетную запись, либо укажите другой номер мобильного телефона.";
    private static final String WARNING_PHONE_USED_FOR_CURRENT_PROFILE = "Номер мобильного телефона уже используется в учетной записи.";

    private final PersonContactService personContactService;
    private final UserPersonalData userPersonalData;

    @Override
    public ComponentType getType() {
        return ComponentType.PhoneNumberChangeInput;
    }


    @Override
    public List<ValidationRule> getValidations() {
        return Arrays.asList(
                new NotBlankValidation("Введите корректный номер телефона"),
                new RegExpValidation()
        );
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, String key, String phoneNumber) {
        String phoneNumberForEsia = personContactService.preparePhoneNumberForEsia(phoneNumber);
        if (phoneNumberForEsia.equals(userPersonalData.getVerifiedPhoneNumber())){
            incorrectAnswers.put(key, WARNING_PHONE_USED_FOR_CURRENT_PROFILE);
            return;
        }
        EsiaContactState phoneUsedResponse = personContactService.validatePhoneNumber(phoneNumberForEsia);
        if (phoneUsedResponse.isState()) {
            long daysPassedAfterPhoneChange = ChronoUnit.DAYS.between(LocalDate.parse(phoneUsedResponse.getVerifiedOn(), DATE_FORMATTER), LocalDate.now());
            if (daysPassedAfterPhoneChange < 30) {
                incorrectAnswers.put(key, WARNING_PHONE_USED_IN_LAST_30_DAYS);
            }
        }
    }

    @Override
    protected void postProcess(FieldComponent component, ScenarioDto scenarioDto, String phoneNumber) {
        String phoneNumberForEsia = personContactService.preparePhoneNumberForEsia(phoneNumber);
        personContactService.updatePhoneNumber(phoneNumberForEsia, scenarioDto);
    }
}
