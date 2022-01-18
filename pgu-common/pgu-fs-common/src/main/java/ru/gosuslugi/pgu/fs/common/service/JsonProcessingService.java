package ru.gosuslugi.pgu.fs.common.service;


import java.io.File;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.jsonpath.DocumentContext;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import org.springframework.core.io.Resource;

public interface JsonProcessingService {

    String toJson(Object o);

    <T> T fromJson(String json, Class<T> clazz);

    <T> T fromJson(File jsonFile, Class<T> clazz);

    <T> T fromJson(String json, TypeReference<T> typeReference);

    <T> T clone(T t);

    String convertAnswersToJsonString(Map<String, ApplicantAnswer> answerMap);

    <T> T getFieldFromContext(String field, DocumentContext documentContext, Class<T> aClass);

    String componentDtoToString(ComponentResponse<?> componentResponse);

    <T> T fromResource(Resource resource, Class<T> clazz);
}
