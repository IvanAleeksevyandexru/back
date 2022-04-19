package ru.gosuslugi.pgu.fs.common.component.userdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import ru.atc.carcass.security.rest.model.person.Person;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.component.validation.RegExpValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.RequiredNotBlankValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.PersonUserInn;

/**
 * Класс реализует валидацию ИНН физ. лица с данными из ЛК и по регулярным выражениям в json.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PersonUserInnComponent extends AbstractComponent<String> {
    private final UserPersonalData userPersonalData;

    @Override
    public ComponentType getType() {
        return PersonUserInn;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component) {
        Optional<String> optionalInn = Optional.of(userPersonalData).map(UserPersonalData::getPerson).map(Person::getInn);
        if (optionalInn.isEmpty()) {
            return ComponentResponse.empty();
        }
        return ComponentResponse.of(optionalInn.get());
    }

    @Override
    public List<ValidationRule> getValidations() {
        return List.of(
                new RequiredNotBlankValidation("Поле обязательно для заполнения"),
                new RegExpValidation()
        );
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        String innAfterSubmit = AnswerUtil.getValue(entry);
        if (StringUtils.isBlank(innAfterSubmit)) {
            incorrectAnswers.put(entry.getKey(), "ИНН не может быть пустым");
            return;
        }
        ComponentResponse<String> initialResponse = getInitialValue(fieldComponent);
        String inn = initialResponse.get();
        if (!StringUtils.equals(inn, innAfterSubmit)) {
            incorrectAnswers.put(entry.getKey(), String.format("ИНН не совпадает с запрошенным из ЛК: %s", inn));
            return;
        }
    }
}
