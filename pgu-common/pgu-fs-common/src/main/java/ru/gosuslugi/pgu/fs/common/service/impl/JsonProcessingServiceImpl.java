package ru.gosuslugi.pgu.fs.common.service.impl;

import java.io.*;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.PathNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.core.exception.JsonParsingException;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class JsonProcessingServiceImpl implements JsonProcessingService {

    private final ObjectMapper objectMapper;

    @Override
    public String toJson(Object o) {
        String result = "";
        try {
            if(o instanceof String) {
                result = o.toString();
            } else {
                result =  objectMapper.writeValueAsString(o);
            }
        } catch (JsonProcessingException e) {
            throw new JsonParsingException("Error while processing json to string: " + o, e);
        }
        return result;
    }

    @Override
    public <T> T fromJson(String json, Class<T> clazz) {
        T result = null;
        try {
            result = objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new JsonParsingException("Error while processing object from json: " + json + "; class: " + clazz, e);
        }
        return result;
    }

    @Override
    public <T> T fromJson(File jsonFile, Class<T> clazz) {
        T result = null;
        try {
            result = objectMapper.readValue(jsonFile, clazz);
        } catch (IOException e) {
            throw new JsonParsingException("Error while processing object from json: " + jsonFile + "; class: " + clazz, e);
        }
        return result;
    }

    @Override
    public <T> T fromJson(String json, TypeReference<T> typeReference) {
        T result = null;
        try {
            result = objectMapper.readValue(json, typeReference);
        } catch (IOException e) {
            throw new JsonParsingException("Error while processing object from json: " + json + "; class: " + typeReference, e);
        }
        return result;
    }

    @Override
    public <T> T clone(T t) {
        if (isNull(t)) {
            return t;
        }
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(t), (Class<T>) t.getClass());
        } catch (JsonProcessingException e) {
            throw new JsonParsingException("Error while cloning: " + t, e);
        }
    }

    /**
     * Метод для нормализации value в applicantAnswers и currentValue
     * В случае, если в value содержится экранированный сериализованный объект - превратит строку в объект
     * В случае, если в value простая строка - не повлияет на значение
     *
     * @param answerMap
     * @return
     */
    @Override
    public String convertAnswersToJsonString(Map<String, ApplicantAnswer> answerMap) {
        ObjectNode root = objectMapper.createObjectNode();
        for (Map.Entry<String, ApplicantAnswer> answerEntry : answerMap.entrySet()) {
            ApplicantAnswer answer = answerEntry.getValue();
            ObjectNode answerNode = objectMapper.createObjectNode();
            answerNode.put("visited", answer.getVisited());
            if (answer.getValue() != null) {
                if (!StringUtils.hasText(answer.getValue())) {
                    answerNode.put("value", answer.getValue());
                } else {
                    try {
                        // пытаемся распарсить value как сложный объект
                        JsonNode valueNode = objectMapper.readTree(answer.getValue());

                        // Или массив, или Node объект, или однозначное взаимное преобразование
                        // Пример некорректной работы "readTree" без этих проверок: "55 область" преобразуется в 55 (Integer) без ошибок
                        if (
                                valueNode.isArray()
                                        || valueNode.isObject()
                                        || answer.getValue().equals(valueNode.toString())
                        ) {
                            answerNode.set("value", valueNode);
                        } else {
                            answerNode.put("value", answer.getValue());
                        }
                    } catch (JsonParseException e) {
                        // JsonParseException вылетит если value - простая строка
                        answerNode.put("value", answer.getValue());
                    } catch (IOException e) {
                        log.error("Error while converting answers to json", e);
                        throw new JsonParsingException("Error while converting answers to json", e);
                    }
                }
            } else {
                answerNode.putNull("value");
            }

            root.set(answerEntry.getKey(), answerNode);
        }
        return root.toString();
    }

    @Override
    public <T> T getFieldFromContext(String field, DocumentContext documentContext, Class<T> aClass) {
        try {
            return documentContext.read("$." + field, aClass);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    @Override
    public String componentDtoToString(ComponentResponse<?> componentResponse) {
        if (componentResponse == null || componentResponse.get() == null) {
            return "";
        }
        return toJson(componentResponse.get());
    }

    @Override
    public <T> T fromResource(Resource resource, Class<T> aClass) {
        return this.fromJson(this.getStringFromResource(resource), aClass);
    }

    private String getStringFromResource(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
