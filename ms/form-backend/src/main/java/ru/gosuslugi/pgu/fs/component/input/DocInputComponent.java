package ru.gosuslugi.pgu.fs.component.input;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.atc.carcass.security.rest.model.person.Kids;
import ru.atc.carcass.security.rest.model.person.PersonDoc;
import ru.gosuslugi.pgu.common.core.exception.ValidationException;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.components.DateInputComponentUtil;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.components.descriptor.types.DocInputField;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.components.descriptor.types.DocInputField;
import ru.gosuslugi.pgu.common.core.date.util.DateUtil;
import ru.gosuslugi.pgu.fs.common.component.AbstractCycledComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.component.input.model.DocInputDto;
import ru.gosuslugi.pgu.fs.utils.PassportUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DATE_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.EMITTER_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.EXPIRATION_DATE_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.IGNORE_VERIFICATION_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ISSUE_ID_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.NUMBER_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.SERIES_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.TYPE_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.VERIFIED_ATTR;
import static ru.gosuslugi.pgu.components.FieldComponentUtil.FIELDS_KEY;
import static ru.gosuslugi.pgu.components.FieldComponentUtil.VALIDATION_ARRAY_KEY;
import static ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil.jsonNodeToString;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocInputComponent extends AbstractCycledComponent<DocInputDto> {

    public static final String REG_EXP_TYPE = "RegExp";
    public final static String REG_EXP_VALUE = "value";
    public final static String REG_EXP_ERROR_MESSAGE = "errorMsg";
    public final static String REG_EXP_ERROR_DESCRIPTION = "errorDesc";
    public final static String DATE_TYPE = "date";
    public final static String HINT_TYPE = "hint";
    public final static String RF_BRTH_CERT = "RF_BRTH_CERT";

    private final UserPersonalData userPersonalData;

    private static final ObjectMapper objectMapper = JsonProcessingUtil.getObjectMapper();

    @Override
    public ComponentType getType() {
        return ComponentType.DocInput;
    }

    @Override
    public ComponentResponse<DocInputDto> getInitialValue(FieldComponent component, ScenarioDto scenarioDto, ServiceDescriptor currentDescriptor) {
        return ComponentResponse.of(getDocInput(userPersonalData.getDocs(), component));
    }

    @Override
    public ComponentResponse<DocInputDto> getCycledInitialValue(FieldComponent component, Map<String, Object> externalData) {
        String documentType = (String) component.getAttrs().get(TYPE_ATTR);
        List<PersonDoc> personDocs = getCycledPersonDocsByType(documentType, externalData);
        return ComponentResponse.of(getDocInput(personDocs, component));
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        Map<String, String> errors = validate(fieldComponent, AnswerUtil.getValue(entry), scenarioDto);
        if (!errors.isEmpty()) {
            incorrectAnswers.put(entry.getKey(), JsonProcessingUtil.toJson(errors));
        }
    }

    private static Map<String, String> validate(FieldComponent fieldComponent, String value, ScenarioDto scenarioDto) {
        Map<String, String> incorrectAnswers = new HashMap<>();

        if (Objects.nonNull(fieldComponent.getAttrs()) && Objects.nonNull(fieldComponent.getAttrs().get(FIELDS_KEY))) {
            Map<String, DocInputField> fields = objectMapper.convertValue(fieldComponent.getAttrs().get(FIELDS_KEY), new TypeReference<>() {});
            removeHints(fields);
            try {
                validateFieldsByRequired(incorrectAnswers, value, fields);
                validateFieldsByRegExp(incorrectAnswers, value, fields);
                validateDateField(incorrectAnswers, value, fields, scenarioDto);
            } catch (IOException e) {
                throw new ValidationException("Failed to parse document input", e);
            }
        }
        return incorrectAnswers;
    }

    private static void validateFieldsByRequired(Map<String, String> incorrectAnswers, String value, Map<String, DocInputField> fields) throws JsonProcessingException {
        JsonNode documentJson = objectMapper.readTree(value);
        fields.entrySet().stream().filter(entry -> entry.getValue().isRequired()).forEach(entry -> {
            JsonNode jsonObj = documentJson.findValue(entry.getKey());
            String stringToCheck = jsonNodeToString(jsonObj);
            if (stringToCheck == null ||stringToCheck.isBlank()) {
                incorrectAnswers.put(entry.getKey(), "Поле не может быть пустым");
            }
        });
    }

    public static Map<String, String> validateFieldsByRegExp(Map<String, String> incorrectAnswers, String value, Map<String, DocInputField> fields) throws JsonProcessingException {
        JsonNode documentJson = objectMapper.readTree(value);
        for (Map.Entry<String, DocInputField> entry : fields.entrySet()) {
            String fieldName = entry.getKey();
            DocInputField field = entry.getValue();
            Optional.ofNullable(field)
                    .map(DocInputField::getAttrs)
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
                                JsonNode jsonObj = documentJson.findValue(fieldName);
                                String stringToCheck = jsonNodeToString(jsonObj);
                                if (
                                        !incorrectAnswers.containsKey(fieldName)
                                                && (field.isRequired() ? !isNull(stringToCheck) : !StringUtils.isEmpty(stringToCheck))
                                                && !stringToCheck.matches(validationRule.get(REG_EXP_VALUE))
                                ) {
                                    incorrectAnswers.put(fieldName, validationRule.get(REG_EXP_ERROR_MESSAGE));
                                }
                            }
                    );
        }
        return incorrectAnswers;
    }

    private static void validateDateField(Map<String, String> incorrectAnswers, String value, Map<String, DocInputField> fields, ScenarioDto scenarioDto) throws JsonProcessingException {
        JsonNode documentJson = objectMapper.readTree(value);
        fields.entrySet().stream().filter(entry -> DATE_TYPE.equalsIgnoreCase(entry.getValue().getType()) && !incorrectAnswers.containsKey(entry.getKey())).forEach(entry -> {
            JsonNode jsonObj = documentJson.findValue(entry.getKey());
            String stringToCheck = jsonNodeToString(jsonObj);
            FieldComponent dateComponent = FieldComponent.builder()
                    .id(entry.getKey())
                    .type(ComponentType.DateInput)
                    .label(entry.getValue().getLabel())
                    .attrs(entry.getValue().getAttrs())
                    .required(true)
                    .build();
            DateInputComponentUtil.setReferenceValue(scenarioDto,dateComponent);
            DateInputComponentUtil.setMinMaxDates(dateComponent);
            List<Map<String, String>> validationDateList = DateInputComponentUtil.getValidationDateList(dateComponent);
            List<String> validationErrors = DateInputComponentUtil.validate(validationDateList, dateComponent, stringToCheck);
            validationErrors.forEach(error -> {
                if (!incorrectAnswers.containsKey(entry.getKey())) {
                    incorrectAnswers.put(entry.getKey(), error);
                }
            });
        });
    }

    private static void removeHints(Map<String, DocInputField> fields) {
        List<String> hintsKeys = fields.entrySet().stream().filter(entry -> HINT_TYPE.equalsIgnoreCase(entry.getValue().getType())).map(Map.Entry::getKey).collect(Collectors.toList());
        hintsKeys.forEach(fields::remove);
    }

    private DocInputDto getDocInput(List<PersonDoc> personDocs, FieldComponent component) {
        String documentType = (String) component.getAttrs().get(TYPE_ATTR);
        if (documentType == null) {
            if (log.isWarnEnabled()) log.warn("No type attribute defined in {} component definition", component.getId());
            return new DocInputDto();
        }
        Map<String, DocInputField> fields = objectMapper.convertValue(component.getAttrs().get(FIELDS_KEY), new TypeReference<>() {});
        boolean ignoreVerification = component.getBooleanCapableAttr(IGNORE_VERIFICATION_ATTR, Boolean.TRUE);

        DocInputDto docInputDto = getDocInputByType(personDocs, documentType, fields, ignoreVerification);
        if (docInputDto == null) {
            docInputDto = new DocInputDto();
        }

        return docInputDto;
    }

    private List<PersonDoc> getCycledPersonDocsByType(String type, Map<String, Object> context) {
        if (RF_BRTH_CERT.equals(type)) {
            List<Kids> kids = userPersonalData.getKids().stream()
                .filter(it -> context.get("id").toString().equals(it.getId()))
                .collect(Collectors.toList());
            if (!kids.isEmpty()) {
                Kids kid = kids.get(0);
                return kid.getDocuments().getDocs();
            }
        }

        return new ArrayList<>();
    }

    private DocInputDto getDocInputByType(List<PersonDoc> docs, String documentType, Map<String, DocInputField> fields, boolean ignoreVerification) {
        return docs.stream()
            .filter(x -> (x.getType().equals(documentType) && (ignoreVerification || x.getVrfStu().equals(VERIFIED_ATTR))))
            .findFirst()
            .map(document -> {
                DocInputDto result = new DocInputDto();
                if (fields.containsKey(SERIES_ATTR)) {
                    result.setSeries(document.getSeries());
                }
                if (fields.containsKey(NUMBER_ATTR)) {
                    result.setNumber(document.getNumber());
                }
                if (fields.containsKey(DATE_ATTR)) {
                    result.setDate(DateUtil.toOffsetDateTimeString(document.getIssueDate(), DateUtil.ESIA_DATE_FORMAT));
                }
                if (fields.containsKey(EMITTER_ATTR)) {
                    result.setEmitter(document.getIssuedBy());
                }
                if (fields.containsKey(ISSUE_ID_ATTR)) {
                    result.setIssueId(PassportUtil.formatIssueId(document.getIssueId()));
                }
                if (fields.containsKey(EXPIRATION_DATE_ATTR)) {
                    result.setExpirationDate(DateUtil.toOffsetDateTimeString(document.getExpiryDate(), DateUtil.ESIA_DATE_FORMAT));
                }
                return result;
            })
            .orElse(null);
    }


}
