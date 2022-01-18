package ru.gosuslugi.pgu.fs.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.internal.ParseContextImpl;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import net.minidev.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.gosuslugi.pgu.common.core.exception.JsonParsingException;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static java.util.Objects.isNull;

public class ContextJsonUtil<T> {
    private static final String ROOT_PATH = "$";

    private static final ObjectMapper OBJECT_MAPPER = JsonProcessingUtil.getObjectMapper();
    private static final ParseContextImpl PARSE_CONTEXT = new ParseContextImpl(
        Configuration.builder().mappingProvider(new JacksonMappingProvider(OBJECT_MAPPER)).build()
    );

    private final ApplicantAnswer applicantAnswer;
    private final String path;
    private final Class<T> clazz;
    private final DocumentContext entryValueContext;

    public ContextJsonUtil(ApplicantAnswer applicantAnswer, String path, Class<T> clazz) {
        this.applicantAnswer = applicantAnswer;
        this.path = path;
        this.clazz = clazz;

        this.entryValueContext = Optional.ofNullable(this.applicantAnswer)
            .map(ApplicantAnswer::getValue)
            .map(PARSE_CONTEXT::parse)
            .orElse(null);
    }

    public T read() {
        return Optional.ofNullable(entryValueContext)
            .map(context -> context.read(path, clazz))
            .orElse(null);
    }

    public void save(T field) {
        if (!isNull(applicantAnswer) && !isNull(entryValueContext)) {

            String toSave;
            // DocumentContext is not supported updating root. In this case: using of JsonProcessingUtil.toJson
            if (ROOT_PATH.equals(path)) {
                toSave = JsonProcessingUtil.toJson(field);
            } else {
                entryValueContext.set(path, field);
                toSave = entryValueContext.jsonString();
            }
            applicantAnswer.setValue(toSave);
        }
    }

    /**
     * Осуществляет поиск указанного в параметрах элемента в json с любой вложенностью.
     * Сначала проверяет без вложенности сразу под корневым элементов в entry, затем в глубину, если первый поиск завершился с исключением.
     * @param jsonProcessingService сервис для работы с json
     * @param entry одна нода ответа, записываемая в сценарий
     * @param jsonNodeName имя ноды, под которой осуществляется поиск
     * @param attributeName имя атрибута, под которым ищем значение
     * @return значение в искомом атрибуте
     */
    public static Entry<String, String> getAttributeInDepth(JsonProcessingService jsonProcessingService, Entry<String, ApplicantAnswer> entry,
                                                            String jsonNodeName, String attributeName) {
        String selectedValue = "";
        try {
            JSONObject valueJson = new JSONObject(AnswerUtil.getValue(entry));
            if (jsonNodeName != null) {
                JSONObject value = valueJson.getJSONObject(jsonNodeName);
                selectedValue = value.getString(attributeName);
            } else {
                selectedValue = valueJson.getString(attributeName);
            }
        } catch (JSONException e) {
            DocumentContext dc = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(Map.ofEntries(entry)));
            Object value;
            try {
                value = dc.read("$.." + attributeName);
                selectedValue = (value instanceof JSONArray) ? ((JSONArray) value).get(0).toString() : value.toString();
            } catch (PathNotFoundException ex) {
                throw new JsonParsingException("Ошибка поиска элемента value в ApplicantAnswer", ex);
            }
        }
        return Map.entry(attributeName, selectedValue);
    }
}
