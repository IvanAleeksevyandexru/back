package ru.gosuslugi.pgu.fs.component.userdata;

import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.core.date.util.DateUtil;
import ru.gosuslugi.pgu.fs.common.exception.ErrorModalException;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseWorkflowException;
import ru.gosuslugi.pgu.common.core.exception.JsonParsingException;
import ru.gosuslugi.pgu.common.esia.search.exception.MultiplePersonFoundException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.PersonWithAge;
import ru.gosuslugi.pgu.common.esia.search.service.PersonSearchService;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ApplicantAnswerItem;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractCycledComponent;
import ru.gosuslugi.pgu.fs.common.component.validation.NotBlankValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalDescriptorService;
import ru.gosuslugi.pgu.fs.descriptor.ErrorModalView;
import ru.gosuslugi.pgu.fs.service.ParticipantService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static ru.gosuslugi.pgu.components.ComponentAttributes.BIRTH_DATE_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.COMPARE_ROWS_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.COMPARE_ROWS_SCREENS_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.FIRST_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.GENDER_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.LAST_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.MIDDLE_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.RF_PASSPORT_NUMBER_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.RF_PASSPORT_SERIES_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.SNILS;

@Component
@RequiredArgsConstructor
public class PassportLookupComponent extends AbstractCycledComponent<String> {

    public final static String USER_OID_ATTR = "oid";
    private final static String COMPARE_ROWS_ERROR_TEXT = "<div class=\"text_modal_error\">\n<img style=\"display:block; margin: 24px auto\" src=\"{staticDomainAssetsPath}/assets/icons/svg/warn.svg\">\n<h4>Не найдена учётная запись</h4>\n<span>Проверьте указанные ФИО и паспортные данные.</span></div>";

    private final PersonSearchService personSearch;
    private final ParticipantService participantService;
    private final ErrorModalDescriptorService errorModalDescriptorService;

    @Override
    public ComponentType getType() {
        return ComponentType.PassportLookup;
    }

    @Override
    public List<ValidationRule> getValidations() {
        return List.of(
                new NotBlankValidation("Серия паспорта не задана")
        );
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        Map<Object, Object> valueMap = AnswerUtil.toMap(entry, true);
        if (!valueMap.containsKey(RF_PASSPORT_SERIES_ATTR) || StringUtils.isEmpty(valueMap.get(RF_PASSPORT_SERIES_ATTR))) {
            incorrectAnswers.put(entry.getKey(), "Серия паспорта не задана");
        }
        if (!valueMap.containsKey(RF_PASSPORT_NUMBER_ATTR) || StringUtils.isEmpty(valueMap.get(RF_PASSPORT_NUMBER_ATTR))) {
            incorrectAnswers.merge(entry.getKey(), "Номер паспорта не задан", (v1, v2) -> "Серия и номер паспорта не заданы");
        }
    }

    @Override
    protected void postProcess(Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        Map<Object, Object> valueMap = AnswerUtil.toMap(entry, true);

        String passportSeries = valueMap.get(RF_PASSPORT_SERIES_ATTR).toString();
        String passportNumber = valueMap.get(RF_PASSPORT_NUMBER_ATTR).toString();
        PersonWithAge person;
        try {
            person = personSearch.searchOneTrusted(passportSeries, passportNumber);
        } catch (MultiplePersonFoundException e) {
            throw new ErrorModalException(errorModalDescriptorService.getErrorModal(ErrorModalView.PASSPORT), e);
        }
        if (person == null) {
            person = new PersonWithAge();
            person.setExists(false);
        } else {
            // проверяем если требуется соответствие данных найденных по паспорту и введенных ранее
            checkPersonByAnswers(person, scenarioDto, fieldComponent);
            person.setExists(true);
            refreshParticipants(fieldComponent, scenarioDto, person, entry);
            valueMap.put(USER_OID_ATTR, person.getOid());
            valueMap.put(SNILS, person.getSnils());
            valueMap.put(FIRST_NAME_ATTR, person.getFirstName());
            valueMap.put(LAST_NAME_ATTR, person.getLastName());
            valueMap.put(MIDDLE_NAME_ATTR, Optional.ofNullable(person.getMiddleName()).orElse(""));
            if (!StringUtils.isEmpty(person.getBirthDate())) {
                String birthDate = DateUtil.convertEsiaDateToISODate(person.getBirthDate());
                valueMap.put(BIRTH_DATE_ATTR, birthDate);
            }
            valueMap.put(GENDER_ATTR, person.getGender());
        }
        valueMap.put("exists", person.isExists());
        entry.getValue().setValue(JsonProcessingUtil.toJson(valueMap));
    }

    @Override
    public void addToCycledItemEsiaData(FieldComponent fieldComponent, ApplicantAnswer applicantAnswer, CycledApplicantAnswerItem answerItem) {
        try {
            if (Objects.nonNull(applicantAnswer) && !StringUtils.isEmpty(applicantAnswer.getValue())) {
                JSONObject valueJson = new JSONObject(applicantAnswer.getValue());
                List<String> keys = Optional.ofNullable(JSONObject.getNames(valueJson))
                        .map(Arrays::asList)
                        .orElse(Collections.emptyList());

                boolean containsMiddleName = false;

                for (String key : keys) {
                    answerItem.getEsiaData().put(key, valueJson.get(key));
                    answerItem.getFieldToId().put(key, fieldComponent.getId());
                    if (MIDDLE_NAME_ATTR.equals(key)) {
                        containsMiddleName = true;
                    }
                }
                // Дополнительно обрабатываем отчество так как оно может быть пустым и не попасть в json
                if (!containsMiddleName) {
                    answerItem.getEsiaData().put(MIDDLE_NAME_ATTR, null);
                    answerItem.getFieldToId().put(MIDDLE_NAME_ATTR, fieldComponent.getId());
                }
            }
        } catch (JSONException e) {
            throw new JsonParsingException("Failed to convert applicant answer to json", e);
        }
    }

    private void checkPersonByAnswers(PersonWithAge person, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        if (fieldComponent.getAttrs().containsKey(COMPARE_ROWS_ATTR) && fieldComponent.getAttrs().containsKey(COMPARE_ROWS_SCREENS_ATTR)) {
            // проверяем нужно ли для этого экрана делать сравнение полей
            List<String> displays = (List<String>) fieldComponent.getAttrs().get(COMPARE_ROWS_SCREENS_ATTR);
            if (!displays.contains(scenarioDto.getDisplay().getId())) {
                return;
            }

            // сравниваем по полям
            Map<String, String> rows = (Map<String, String>) fieldComponent.getAttrs().get(COMPARE_ROWS_ATTR);
            rows.forEach((key, value) -> {
                String personRowValue = null;
                switch (key) {
                    case FIRST_NAME_ATTR:
                        personRowValue = person.getFirstName();
                        break;
                    case LAST_NAME_ATTR:
                        personRowValue = person.getLastName();
                        break;
                }

                // упрощенное получение ответа
                ApplicantAnswer answer = scenarioDto.getApplicantAnswers().get(value);
                String answerValue = null;
                if (answer != null) {
                    answerValue = answer.getValue();
                }

                if (personRowValue == null || !personRowValue.equalsIgnoreCase(answerValue)) {
                    throw new FormBaseWorkflowException(COMPARE_ROWS_ERROR_TEXT);
                }
            });
        }
    }

    private void refreshParticipants(FieldComponent fieldComponent, ScenarioDto scenarioDto, PersonWithAge person, Map.Entry<String, ApplicantAnswer> entry) {
        boolean isNewParticipantAddStrategy = fieldComponent.getAttrs().containsKey("mainCycledComponentId");
        if (isNewParticipantAddStrategy) {
            participantService.addParticipant(scenarioDto, fieldComponent, person, getCurrentIndex(scenarioDto, entry, fieldComponent));
            return;
        }
        participantService.setParticipant(scenarioDto, fieldComponent, person, getCurrentIndex(scenarioDto, entry, fieldComponent));
    }

    private Integer getCurrentIndex(ScenarioDto scenarioDto, Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        if (entry.getValue() instanceof ApplicantAnswerItem)
            return ((ApplicantAnswerItem) entry.getValue()).getIndex();

        String mainCycledComponentId = fieldComponent.getAttrs().getOrDefault("mainCycledComponentId", "").toString();
        if (StringUtils.isEmpty(mainCycledComponentId))
            return null;

        var currentCyclicAnswer = scenarioDto.getCycledApplicantAnswers().getCurrentAnswer();

        if (Objects.nonNull(currentCyclicAnswer) && mainCycledComponentId.equals(currentCyclicAnswer.getId())) {
            var currentAnswerItem = currentCyclicAnswer.getCurrentAnswerItem();
            int index = 0;
            for (var item: currentCyclicAnswer.getItems()) {
                if (item.getId().equals(currentAnswerItem.getId())) return index;
                index++;
            }
        }
        return null;
    }

}
