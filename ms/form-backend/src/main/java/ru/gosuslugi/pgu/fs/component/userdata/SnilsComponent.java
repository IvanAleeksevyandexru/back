package ru.gosuslugi.pgu.fs.component.userdata;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.esia.search.exception.MultiplePersonFoundException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.PersonWithAge;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.common.esia.search.service.PersonSearchService;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ApplicantAnswerItem;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswer;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.components.ComponentAttributes;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractCycledComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.exception.ErrorModalException;
import ru.gosuslugi.pgu.fs.common.service.InitialValueFromService;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.common.utils.StringConvertHelper;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalDescriptorService;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalView;
import ru.gosuslugi.pgu.fs.service.ParticipantService;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang.StringUtils.isBlank;
import static ru.gosuslugi.pgu.components.ComponentAttributes.SNILS;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnilsComponent extends AbstractCycledComponent<String> {

    private static final String VALIDATION_ARRAY_KEY = "validation";
    private static final String REG_EXP_ERROR_MESSAGE = "errorMsg";
    private static final String DEFAULT_ERROR_MESSAGE = "Неверное значение СНИЛС";
    private static final String REPEATED_CHILDREN_SNILS_ERROR_MESSAGE = "У детей не могут быть одинаковые СНИЛС";

    private static final String VALIDATION_OFF_ATTR = "validationOff";

    private static final String SNILS_REG_EXP = "\\d{3}-\\d{3}-\\d{3}[ ]{1}\\d{2}";

    private final PersonSearchService personSearch;

    private final JsonProcessingService jsonProcessingService;
    private final ParticipantService participantService;
    private final UserPersonalData userPersonalData;
    private final InitialValueFromService initialValueFromService;
    private final ErrorModalDescriptorService errorModalDescriptorService;

    @Override
    public ComponentType getType() {
        return ComponentType.SnilsInput;
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        String presetValue = initialValueFromService.getValue(component, scenarioDto);
        if (!isBlank(presetValue)) {
            return ComponentResponse.of(presetValue);
        }
        return ComponentResponse.of(component.getValue());
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        String value = AnswerUtil.getValue(entry);
        if (StringUtils.isEmpty(value) && fieldComponent.isRequired()) {
            setNewAnswerValue(initPersonWithAge(entry.getValue().getValue()), entry, fieldComponent);
            incorrectAnswers.put(entry.getKey(), getErrorMessage(fieldComponent, DEFAULT_ERROR_MESSAGE));
        }
        if (StringUtils.isEmpty(value)) {
            return;
        }

        if (!value.matches(SNILS_REG_EXP)) {
            setNewAnswerValue(initPersonWithAge(entry.getValue().getValue()), entry, fieldComponent);
            incorrectAnswers.put(entry.getKey(), getErrorMessage(fieldComponent, DEFAULT_ERROR_MESSAGE));
            return;
        }

        String validationOffString = fieldComponent.getAttrs().containsKey(VALIDATION_OFF_ATTR) ?
                fieldComponent.getAttrs().get(VALIDATION_OFF_ATTR).toString() : "false";
        boolean validationOff = validationOffString.isBlank() || "true".equalsIgnoreCase(validationOffString);
        if (!validationOff) {
            if (!checkSumValidation(entry.getValue().getValue())) {
                setNewAnswerValue(initPersonWithAge(entry.getValue().getValue()), entry, fieldComponent);
                incorrectAnswers.put(entry.getKey(), getErrorMessage(fieldComponent, DEFAULT_ERROR_MESSAGE));
                return;
            }
        }
        // Проверка на совпадающие СНИЛС у детей
        if ("true".equals(fieldComponent.getAttrs().get("checkRepeatedChildrenSnils"))) {
            checkRepeatedChildrenSnils(incorrectAnswers, entry, scenarioDto);
        }
        PersonWithAge ps;
        try {
            ps = personSearch.searchOneTrusted(value);
        } catch (MultiplePersonFoundException e) {
            throw new ErrorModalException(errorModalDescriptorService.getErrorModal(ErrorModalView.SNILS), e);
        }
        if (ps != null) {
            if (log.isInfoEnabled()) {
                log.info("Персона по СНИЛСу {} найдена: {}", value, jsonProcessingService.toJson(ps));
            }
            // Что бы избежать зацикливания в participants, но есть случаи, когда по бизнесу-логике нужно.
            // Например 10000000318 услуга - директор сам может быть инженером с минстроевским сертификатом, поэтому вводит себя
            boolean validationOwnSnils = Boolean.parseBoolean((String) fieldComponent.getAttrs().getOrDefault("validationOwnSnils", "true"));
            if (validationOwnSnils && ps.getOid().equals(valueOf(userPersonalData.getUserId()))) {
                setNewAnswerValue(ps, entry, fieldComponent);
                incorrectAnswers.put(entry.getKey(), "Нельзя ввести собственный СНИЛС");
                return;
            }
            ps = filterPerson(ps);
            ps.setExists(true);
            participantService.setParticipant(scenarioDto, fieldComponent, ps,
                    entry.getValue() instanceof ApplicantAnswerItem ? ((ApplicantAnswerItem) entry.getValue()).getIndex() : null);
        } else {
            // сохраняем введенный СНИЛС для дальнейшей отправки приглашения по услуге (компонент InvitationError)
            if (log.isInfoEnabled()) {
                log.info("Персона по СНИЛСу {} не найдена: сохраняем введенный СНИЛС для дальнейшей отправки приглашения по услуге (компонент InvitationError)", value);
            }
            ps = initPersonWithAge(value);
        }
        setNewAnswerValue(ps, entry, fieldComponent);
    }

    private void checkRepeatedChildrenSnils(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto) {
        String componentId = entry.getKey();
        String snils = entry.getValue().getValue();
        List<CycledApplicantAnswer> answers = scenarioDto.getCycledApplicantAnswers().getAnswers();
        answers.forEach(answer -> {
            List<CycledApplicantAnswerItem> items = answer.getItems();                      // дети
            items.stream().filter(el -> nonNull(el.getItemAnswers().get(componentId))).forEach(item -> {
                ApplicantAnswer applicantAnswer = item.getItemAnswers().get(componentId);
                Map<String, String> valueMap = (Map<String, String>) AnswerUtil.tryParseToMap(applicantAnswer.getValue());
                String childSnils = valueMap.get(ComponentAttributes.SNILS);
                if (snils.equals(childSnils)) {
                    incorrectAnswers.put(componentId, REPEATED_CHILDREN_SNILS_ERROR_MESSAGE);
                    return;
                }
            });
        });
    }

    private PersonWithAge initPersonWithAge(String snils) {
        PersonWithAge ps = new PersonWithAge();
        ps.setSnils(snils);
        ps.setExists(false);
        return ps;
    }

    private void setNewAnswerValue(PersonWithAge ps, Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        String jsonString = jsonProcessingService.toJson(ps);
        fieldComponent.setValue(jsonString);

        ApplicantAnswer answer = entry.getValue();
        answer.setValue(jsonString);
        entry.setValue(answer);
    }

    private PersonWithAge filterPerson(PersonWithAge person) {
        PersonWithAge filteredPerson = new PersonWithAge();
        filteredPerson.setTrusted(person.getTrusted());
        filteredPerson.setBirthDate(person.getBirthDate());
        filteredPerson.setCitizenship(person.getCitizenship());
        filteredPerson.setGender(person.getGender());
        filteredPerson.setOid(person.getOid());
        filteredPerson.setSnils(person.getSnils());

        return filteredPerson;
    }

    /**
     * Получить сообщение об ошибке
     *
     * @param fieldComponent описание поля
     * @param defaultMessage сообщение по умолчанию
     * @return текст сообщения об ошибке
     */
    public String getErrorMessage(FieldComponent fieldComponent, String defaultMessage) {
        Optional<String> errorMsg = Optional.ofNullable((List<Map<String, String>>) fieldComponent.getAttrs().get(VALIDATION_ARRAY_KEY))
                .stream()
                .flatMap(Collection::stream).findFirst()
                .stream()
                .findFirst().map(validationRule -> validationRule.get(REG_EXP_ERROR_MESSAGE));
        return errorMsg.orElse(defaultMessage);
    }

    /**
     * Валидация контрольной суммы СНИЛС
     *
     * @param value СНИЛС
     * @return false - валидация не пройдкна
     */
    public boolean checkSumValidation(String value) {
        List<Integer> snilsFigures = StringConvertHelper.getFiguresListFromString(value);
        if (snilsFigures.size() != 11) {
            return false;
        }
        List<Integer> checkFigures = snilsFigures.subList(0, 9);
        Long checkedValue = Long.parseLong(checkFigures.stream().map(String::valueOf).collect(Collectors.joining()));
        if (checkedValue <= 1001998) return true;
        List<Integer> multipleFigures = new ArrayList<>(9);
        for (int i = 0; i < checkFigures.size(); i++) {
            multipleFigures.add((9 - i) * checkFigures.get(i));
        }
        int sumToCheck = multipleFigures.stream().mapToInt(Integer::intValue).sum();
        int calcValue = 0;
        if (sumToCheck < 100) {
            calcValue = sumToCheck;
        }
        if (sumToCheck > 100) {
            calcValue = sumToCheck % 101;
            if (calcValue == 100) {
                calcValue = 0;
            }
        }
        String controlValue = snilsFigures.subList(9, 11).stream().map(String::valueOf).collect(Collectors.joining());
        return String.format("%02d", calcValue).equals(controlValue);
    }

    @Override
    public void addToCycledItemEsiaData(FieldComponent fieldComponent, ApplicantAnswer applicantAnswer, CycledApplicantAnswerItem answerItem) {
        if (applicantAnswer != null && !StringUtils.isEmpty(applicantAnswer.getValue())) {
            Map<String, Object> valueMap = JsonProcessingUtil.fromJson(applicantAnswer.getValue(), new TypeReference<>() {});
            if (valueMap.containsKey(SNILS)) {
                answerItem.getEsiaData().put(SNILS, valueMap.get(SNILS));
                answerItem.getFieldToId().put(SNILS, fieldComponent.getId());
            }
        }

    }
}
