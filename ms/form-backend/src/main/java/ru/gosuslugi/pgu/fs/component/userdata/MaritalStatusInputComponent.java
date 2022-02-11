package ru.gosuslugi.pgu.fs.component.userdata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.core.exception.ValidationException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.components.DateInputComponentUtil;
import ru.gosuslugi.pgu.components.descriptor.types.DocInputField;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.pgu.client.PguMaritalClient;
import ru.gosuslugi.pgu.fs.pgu.dto.MaritalResponseItem;
import ru.gosuslugi.pgu.fs.pgu.dto.MaritalStatusInputResponseDto;
import ru.gosuslugi.pgu.fs.service.DictionaryFilterService;
import ru.gosuslugi.pgu.fs.utils.DateTimeUtil;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiSuggestDictionaryItem;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil.jsonNodeToString;
import static ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil.toJson;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DICTIONARY_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DICT_URL_TYPE;
import static ru.gosuslugi.pgu.components.FieldComponentUtil.FIELDS_KEY;
import static ru.gosuslugi.pgu.components.FieldComponentUtil.VALIDATION_ARRAY_KEY;
import static ru.gosuslugi.pgu.components.regex.RegExpContext.matchesByRegex;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaritalStatusInputComponent extends AbstractComponent<MaritalStatusInputResponseDto> {

    /** Определят сертификат о браке или разводе. */
    private static final String DOCUMENT_TYPE = "documentType";
    /** Орган ЗАГС, зарегистрировавший брак или развод. */
    private static final String ACT_REC_REGISTRATOR = "act_rec_registrator";
    private static final ObjectMapper objectMapper = JsonProcessingUtil.getObjectMapper();

    public static final  String HINT_TYPE = "hint";
    public static final String REG_EXP_TYPE = "RegExp";
    public static final String REG_EXP_VALUE = "value";
    public static final String REG_EXP_ERROR_MESSAGE = "errorMsg";
    public static final String DATE_TYPE = "date";
    public static final String ATTRIBUTE_NAME_FOR_CHECK = "fullname";
    public static final String FNS_ZAGS_ALL = "FNS_ZAGS_ALL";
    public static final String NSI_SUGGEST = "nsiSuggest";
    private final UserPersonalData userPersonalData;
    private final PguMaritalClient pguMarriageClient;
    private final DictionaryFilterService dictionaryFilterService;

    @Override
    public ComponentType getType() {
        return ComponentType.MaritalStatusInput;
    }
    private final NsiDictionaryService nsiDictionaryService;

    @Override
    public ComponentResponse<MaritalStatusInputResponseDto> getInitialValue(FieldComponent fieldComponent) {
        //DTO для возврата сертификата о браке/разводе на фронт
        MaritalStatusInputResponseDto maritalStatusInputResponseDto = new MaritalStatusInputResponseDto();

        if (fieldComponent.getAttrs() != null && fieldComponent.getAttrs().containsKey(DOCUMENT_TYPE)) {
            String maritalStatusType = String.valueOf(fieldComponent.getAttrs().get(DOCUMENT_TYPE));
            //достаем все сертификаты из ЛК
            List<MaritalResponseItem> list = pguMarriageClient.getMaritalStatusCertificate(userPersonalData.getToken(), userPersonalData.getUserId(), maritalStatusType);

            if (!list.isEmpty()) {
                //достаем последний по дате актовой записи сертификат
                var maritalResponseItem = Collections.max(list, Comparator.comparing(MaritalResponseItem::getActDate));
                String zagsName = maritalResponseItem.getIssuedBy();
                if(Objects.nonNull(zagsName)) {
                    fieldComponent.getAttrs().put(DICTIONARY_NAME_ATTR, FNS_ZAGS_ALL);
                    fieldComponent.getAttrs().put(DICT_URL_TYPE, NSI_SUGGEST);
                    String dictionaryName = fieldComponent.getAttrs().get(DICTIONARY_NAME_ATTR).toString();
                    //проверяем, что подтянутый из ЛК ЗАГС есть в nsi-suggest справочнике "FNS_ZAGS_ALL", если нет - запрос сделает фронтэнд
                    var originalItem = nsiDictionaryService.getNsiDictionaryItemByValue(dictionaryName, ATTRIBUTE_NAME_FOR_CHECK, zagsName);
                    if(!CollectionUtils.isEmpty(originalItem)
                            && String.valueOf(originalItem.get(ATTRIBUTE_NAME_FOR_CHECK)).equals(zagsName)) {
                        //объект для возврата органа ЗАГС act_rec_registrator на фронт в требуемом виде
                        var nsiSuggestDictionaryItem = new NsiSuggestDictionaryItem();
                        nsiSuggestDictionaryItem.setOriginalItem(originalItem);
                        nsiSuggestDictionaryItem.setId(String.valueOf(originalItem.get("code")));
                        nsiSuggestDictionaryItem.setText(String.valueOf(originalItem.get(ATTRIBUTE_NAME_FOR_CHECK)));

                        maritalStatusInputResponseDto.setAct_rec_registrator(nsiSuggestDictionaryItem);
                    }
                }
                maritalStatusInputResponseDto.setAct_rec_date(maritalResponseItem.getActDate());
                maritalStatusInputResponseDto.setAct_rec_number(maritalResponseItem.getActNo());
                maritalStatusInputResponseDto.setSeries(maritalResponseItem.getSeries());
                maritalStatusInputResponseDto.setNumber(maritalResponseItem.getNumber());
                String issueDate =  String.valueOf(DateTimeUtil.parseDate(maritalResponseItem.getIssueDate(), "dd.MM.yyyy"));
                maritalStatusInputResponseDto.setIssueDate(issueDate);

                return ComponentResponse.of(maritalStatusInputResponseDto);
            }
        }
        log.warn("Не задан documentType в json-е для получения сертификата");
        return ComponentResponse.empty();
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {

        Map<String, String> errors = validate(fieldComponent, AnswerUtil.getValue(entry), scenarioDto);
        if (!errors.isEmpty()) {
            incorrectAnswers.put(entry.getKey(), JsonProcessingUtil.toJson(errors));
            return;
        }
        if (StringUtils.hasText(AnswerUtil.getValue(entry))) {
            DocumentContext documentContext = JsonPath.parse(AnswerUtil.getValue(entry));
            LinkedHashMap editedZagsObject = jsonProcessingService.getFieldFromContext(ACT_REC_REGISTRATOR, documentContext, LinkedHashMap.class);
            String editedZags = String.valueOf(editedZagsObject.get("text"));
            if(Objects.nonNull(editedZags)) {
                fieldComponent.getAttrs().put(DICTIONARY_NAME_ATTR, FNS_ZAGS_ALL);
                fieldComponent.getAttrs().put(DICT_URL_TYPE, NSI_SUGGEST);
                dictionaryFilterService.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent, () -> ComponentResponse.of(editedZags));
            }
        }
    }

    private static Map<String, String> validate(FieldComponent fieldComponent, String value, ScenarioDto scenarioDto) {
        Map<String, String> incorrectAnswers = new HashMap<>();
        if (Objects.nonNull(fieldComponent.getAttrs()) && Objects.nonNull(fieldComponent.getAttrs().get(FIELDS_KEY))) {
            List<DocInputField> fieldslist = objectMapper.convertValue(fieldComponent.getAttrs().get(FIELDS_KEY), new TypeReference<>() {});
            Map<String, DocInputField> fields = fieldslist.stream()
                    .collect(Collectors.toMap(DocInputField::getFieldName, Function.identity()));
            removeHints(fields);
            try {
                validateFieldsByRequired(incorrectAnswers, value, fields);
                validateFieldsByRegExp(incorrectAnswers, value, fields);
                validateDateField(incorrectAnswers, value, fields, scenarioDto);
            } catch (IOException e) {
                throw new ValidationException("Failed to parse marital status fields", e);
            }
        }
        return incorrectAnswers;
    }

    private static void validateFieldsByRequired(Map<String, String> incorrectAnswers, String value, Map<String, DocInputField> fields) throws JsonProcessingException {
        JsonNode documentJson = objectMapper.readTree(value);
        fields.entrySet().stream()
                .forEach(entry -> {
                    JsonNode jsonObj = documentJson.findValue(entry.getKey());
                    if(Objects.isNull(jsonObj)) {
                        incorrectAnswers.put(entry.getKey(), "Поле не может быть пустым");
                    }
                    //заменить на новое доставание из оригиналайтем
                    String stringToCheck = entry.getKey().equals(ACT_REC_REGISTRATOR) ? toJson(jsonObj) : jsonNodeToString(jsonObj);
                    if (stringToCheck == null || stringToCheck.isBlank() || stringToCheck.equals("null")) {
                        incorrectAnswers.put(entry.getKey(), "Поле не может быть пустым");
                    }
                });
    }

    // TODO Заменить на общую валидацию регулярных выражений
    public static Map<String, String> validateFieldsByRegExp(Map<String, String> incorrectAnswers, String value, Map<String, DocInputField> fields) throws JsonProcessingException {
        JsonNode documentJson = objectMapper.readTree(value);
        fields.forEach((k, v) -> Optional.ofNullable(v)
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
                            JsonNode jsonObj = documentJson.findValue(k);
                            String stringToCheck = jsonNodeToString(jsonObj);
                            if (
                                    !incorrectAnswers.containsKey(k)
                                            && !isNull(stringToCheck)
                                            && !matchesByRegex(stringToCheck, validationRule.get(REG_EXP_VALUE))
                            ) {
                                incorrectAnswers.put(k, validationRule.get(REG_EXP_ERROR_MESSAGE));
                            }
                        }
                ));
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
        List<String> hintsKeys = fields.entrySet().stream()
                .filter(entry -> HINT_TYPE.equalsIgnoreCase(entry.getValue().getType()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        hintsKeys.forEach(fields::remove);
    }
}
