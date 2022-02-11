package ru.gosuslugi.pgu.fs.component.confirm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.atc.carcass.security.rest.model.person.PersonDoc;
import ru.gosuslugi.pgu.common.core.exception.ValidationException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.components.descriptor.types.ValidationFieldDto;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.component.confirm.model.ConfirmPersonalOms;
import ru.gosuslugi.pgu.fs.esia.EsiaRestContactDataClient;
import ru.gosuslugi.pgu.fs.esia.dto.EsiaOmsDto;

import java.io.IOException;
import java.util.*;

import static java.util.Objects.isNull;
import static ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil.jsonNodeToString;
import static ru.gosuslugi.pgu.components.FieldComponentUtil.FIELDS_KEY;
import static ru.gosuslugi.pgu.components.FieldComponentUtil.VALIDATION_ARRAY_KEY;
import static ru.gosuslugi.pgu.components.regex.RegExpContext.matchesByRegex;

/**
 * Компонент для отображения и валидации ОМС  пользователя в модальном окне, которое задается в profile-oms-update.json
 * @see ConfirmPersonalPolicyComponent
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmPersonalPolicyChange extends AbstractComponent<ConfirmPersonalOms> {

    private final EsiaRestContactDataClient esiaRestContactDataClient;
    private final UserPersonalData userPersonalData;

    public static final String REG_EXP_TYPE = "RegExp";
    public static final String REG_EXP_VALUE = "value";
    public static final String REG_EXP_ERROR_MESSAGE = "errorMsg";

    @Override
    public ComponentType getType() {
        return ComponentType.ConfirmPersonalPolicyChange;
    }

    @Override
    public ComponentResponse<ConfirmPersonalOms> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        Optional<PersonDoc> optionalConfirmPersonalOms = userPersonalData.getOmsDocument();
        if(optionalConfirmPersonalOms.isPresent()) {
            ConfirmPersonalOms confirmPersonalOms = new ConfirmPersonalOms();
            confirmPersonalOms.setSeries(optionalConfirmPersonalOms.get().getSeries());
            confirmPersonalOms.setNumber(optionalConfirmPersonalOms.get().getNumber());
            return ComponentResponse.of(confirmPersonalOms);
        }
        return ComponentResponse.empty();
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        super.validateAfterSubmit(incorrectAnswers, entry, fieldComponent);
        Map<String, String> errors = new HashMap<>();
        if (Objects.nonNull(fieldComponent.getAttrs()) && Objects.nonNull(fieldComponent.getAttrs().get(FIELDS_KEY))) {
            List<ValidationFieldDto> fields = objectMapper.convertValue(fieldComponent.getAttrs().get(FIELDS_KEY), new TypeReference<>() {});
            try {
                errors = validateFieldsByRegExp(incorrectAnswers, entry.getValue().getValue(), fields);
            } catch (IOException e) {
                throw new ValidationException("Failed to parse OMS field input", e);
            }
        }
        if (!errors.isEmpty()) {
            incorrectAnswers.put(entry.getKey(), JsonProcessingUtil.toJson(errors));
        }
        // Обновление ОМС в ESIA как часть общей  валидации
        if (incorrectAnswers.isEmpty()) {
            try {
                updateEsiaOms(entry);
            } catch (Exception e) {
                if (log.isWarnEnabled()) {//прописать сюда ошиюку от есии
                    log.warn("Полис ОМС не задан", e);
                }
                incorrectAnswers.put(fieldComponent.getId(), "Не удалось обновить данные ОМС. Попробуйте позже.");
            }
        }
    }


    // TODO Заменить на общую валидацию регулярных выражений
    public static Map<String, String> validateFieldsByRegExp(Map<String, String> incorrectAnswers, String value,List<ValidationFieldDto> fields) throws JsonProcessingException {
        JsonNode documentJson = JsonProcessingUtil.getObjectMapper().readTree(value);
        fields.forEach(field -> Optional.ofNullable(field)
                .map(ValidationFieldDto::getAttrs)
                .filter(map -> map.get(VALIDATION_ARRAY_KEY) instanceof List)
                .map(map -> (List<Map<String, String>>) map.get(VALIDATION_ARRAY_KEY))
                .stream()
                .flatMap(Collection::stream)
                .filter(
                        validationRule ->
                                REG_EXP_TYPE.equalsIgnoreCase(validationRule.get("type"))
                                        && StringUtils.hasText(validationRule.get(REG_EXP_VALUE))
                )
                .forEach(
                        validationRule -> {
                            JsonNode jsonObj = documentJson.findValue(field.getFieldName());
                            String stringToCheck = jsonNodeToString(jsonObj);
                            if (
                                    !incorrectAnswers.containsKey(field.getFieldName())
                                            && !isNull(stringToCheck)
                                            && !matchesByRegex(stringToCheck, validationRule.get(REG_EXP_VALUE))
                            ) {
                                incorrectAnswers.put(field.getFieldName(), validationRule.get(REG_EXP_ERROR_MESSAGE));
                            }
                        }
                ));
        return incorrectAnswers;
    }

    private void updateEsiaOms(Map.Entry<String, ApplicantAnswer> entry) {
        EsiaOmsDto esiaOmsDto = new EsiaOmsDto();
        DocumentContext documentContext = JsonPath.parse(AnswerUtil.getValue(entry));
        String editedOms = jsonProcessingService.getFieldFromContext("number", documentContext, String.class);
        esiaOmsDto.setNumber(editedOms);
        esiaRestContactDataClient.changeOms(esiaOmsDto);
    }
}

