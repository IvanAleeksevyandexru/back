package ru.gosuslugi.pgu.fs.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.core.exception.JsonParsingException;
import ru.gosuslugi.pgu.components.FieldComponentUtil;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ApplicantAnswerItem;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswer;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem;
import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswers;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.gosuslugi.pgu.components.FieldComponentUtil.VALIDATION_ARRAY_KEY;
import static ru.gosuslugi.pgu.components.RegExpUtil.REG_EXP_VALUE;

@Slf4j
public class AnswerUtil {

    private AnswerUtil() {
    }

    public static String getValueOrNull(Map.Entry<String, ApplicantAnswer> entry) {
        return getOptionalString(entry).orElse(null);
    }

    @NonNull
    public static String getValue(Map.Entry<String, ApplicantAnswer> entry) {
        return Optional.ofNullable(entry)
                .map(Map.Entry::getValue)
                .map(ApplicantAnswer::getValue)
                .orElse("");
    }

    public static Map<Object, Object> toMap(Map.Entry<String, ApplicantAnswer> entry, boolean elseEmptyMap) {
        return getOptionalString(entry)
            .map(value -> (Map<Object, Object>) JsonProcessingUtil.fromJson(value, Map.class))
            .orElse(elseEmptyMap ? ((Map<Object, Object>) Collections.EMPTY_MAP) : null);
    }

    public static Map<String, String> toStringMap(Map.Entry<String, ApplicantAnswer> entry, boolean elseEmptyMap) {
        return Optional.ofNullable(toMap(entry, elseEmptyMap))
            .map(FieldComponentUtil::toStringMap)
            .orElse(null);
    }

    public static List<Object> toList(Map.Entry<String, ApplicantAnswer> entry, boolean elseEmptyList) {
        Optional<String> valueOptional = getOptionalString(entry);
        if (valueOptional.isPresent() && !valueOptional.get().isBlank()) {
            return (List<Object>) JsonProcessingUtil.fromJson(valueOptional.get(), List.class);
        }
        return elseEmptyList ? ((List<Object>) Collections.EMPTY_LIST) : null;
    }

    public static List<Map<Object, Object>> toMapList(Map.Entry<String, ApplicantAnswer> entry, boolean elseEmptyList) {
        return Optional.ofNullable(toList(entry, elseEmptyList))
            .map(
                list ->
                    list.stream()
                    .filter(item -> item instanceof Map)
                    .map(item -> (Map<Object, Object>) item)
                    .collect(Collectors.toList())
            )
            .orElse(null);
    }

    public static List<Map<String, String>> toStringMapList(Map.Entry<String, ApplicantAnswer> entry, boolean elseEmptyList) {
        return Optional.ofNullable(toMapList(entry, elseEmptyList))
            .map(
                list ->
                    list.stream()
                    .map(FieldComponentUtil::toStringMap)
                    .collect(Collectors.toList())
            )
            .orElse(null);
    }

    public static Map<String, Object> convertAnswersMapToObjectsMap(ObjectMapper mapper, Map<String, ApplicantAnswer> map) {
        Map<String, Object> result = new HashMap<>();
        TypeReference<Object> typeRef = new TypeReference<>() {};

        map.forEach((key, val) -> {
            if(val.getValue() != null && (val.getValue().contains("{") || val.getValue().contains("["))) {
                try {
                    Object innerObject = mapper.readValue(val.getValue(), typeRef);
                    result.put(key, innerObject);
                } catch (JsonProcessingException e) {
                    log.error("Cannot process inner value for application field with id {}, content of the field is: {}. Error: {}", key, val.getValue(), e);
                }
            } else {
                result.put(key, val.getValue());
            }
        });

        return result;
    }

    public static Map.Entry<String, ApplicantAnswer> createAnswerEntry(String key, String value) {
        ApplicantAnswer childrenAnswer = new ApplicantAnswer();
        childrenAnswer.setValue(value);
        childrenAnswer.setVisited(true);
        return new AbstractMap.SimpleEntry<>(key, childrenAnswer);
    }

    public static Map.Entry<String, ApplicantAnswer> createRepeatableItemAnswerEntry(String key, String value, int index) {
        ApplicantAnswerItem childAnswer = new ApplicantAnswerItem();
        childAnswer.setValue(value);
        childAnswer.setVisited(true);
        childAnswer.setIndex(index);
        return new AbstractMap.SimpleEntry<>(key, childAnswer);
    }

    private static Optional<String> getOptionalString(Map.Entry<String, ApplicantAnswer> entry) {
        return Optional.ofNullable(entry).map(Map.Entry::getValue).map(ApplicantAnswer::getValue);
    }

    public static Object tryParseToMap(String value) {
        if (StringUtils.isEmpty(value)) return value;

        var normalized = value.strip();
        if (normalized.startsWith("[") || !normalized.startsWith("{") || !normalized.endsWith("}")) return value;

        try {
            return JsonProcessingUtil.fromJson(value, HashMap.class);
        } catch (JsonParsingException e) {
            return value;
        }
    }

    public static Object tryParseToList(String value) {
        if (StringUtils.isEmpty(value)) return value;

        try {
            return JsonProcessingUtil.fromJson(value, List.class);
        } catch (JsonParsingException e) {
            return value;
        }
    }

    public static void setCycleReferenceValue(ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        Optional.ofNullable((List<Map<String, Object>>) fieldComponent.getAttrs().get(VALIDATION_ARRAY_KEY))
            .orElse(Collections.emptyList())
            .forEach(
                v -> {
                    Optional<Map<String, ApplicantAnswer>> answer = Optional.ofNullable(scenarioDto.getCycledApplicantAnswers())
                        .map(CycledApplicantAnswers::getCurrentAnswer)
                        .map(CycledApplicantAnswer::getCurrentAnswerItem)
                        .map(CycledApplicantAnswerItem::getItemAnswers);
                    v.put(REG_EXP_VALUE, answer.orElse(Collections.emptyMap()));
                }
            );
    }

    public static List<CycledApplicantAnswerItem> toCycledApplicantAnswerItem(List<Map<String, String>> listAnswers) {
        return listAnswers.stream()
                .map(itemAnswers -> {
                    Map<String, ApplicantAnswer> answersMap = itemAnswers.entrySet().stream()
                            .map(entry -> createAnswerEntry(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    CycledApplicantAnswerItem item = new CycledApplicantAnswerItem();
                    item.setItemAnswers(answersMap);
                    return item;
                })
                .collect(Collectors.toList());
    }
}
