package ru.gosuslugi.pgu.fs.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.common.core.exception.JsonParsingException;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.service.ComponentReferenceService;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.service.DictionaryListPreprocessorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static ru.gosuslugi.pgu.components.FieldComponentUtil.DICTIONARY_LIST_KEY;

/**
 * Сервис для получения списка items, заданных в linkedValues из предыдщего компонента
 * и сохранения его в dictionaryList в attrs текущего компонента
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class DictionaryListPreprocessorServiceImpl implements DictionaryListPreprocessorService {

    private static final String DICTIONARY_CODE_KEY = "dictionaryRefCode";
    private static final String DICTIONARY_LABEL_KEY = "dictionaryRefLabel";

    private final ObjectMapper objectMapper;
    private final JsonProcessingService jsonProcessingService;
    private final ComponentReferenceService componentReferenceService;

    @Override
    public void prepareDictionaryListFromComponent(FieldComponent component, ScenarioDto scenarioDto) {
        String value = component.getArguments().get(DICTIONARY_REF_KEY);
        prepareDictionaryListFromComponent(component, scenarioDto, value);
    }
    @Override
    public void prepareDictionaryListFromComponent(FieldComponent component, ScenarioDto scenarioDto, String value) {
        try {
            List<Map> externalDictionaryList = objectMapper.readValue(value, List.class);
            String labelPath = component.getArguments().get(DICTIONARY_LABEL_KEY);
            String codePath = component.getArguments().get(DICTIONARY_CODE_KEY);
            List<Map<String,Object>> items = new ArrayList<>();
            for (Object extDictionaryItem : externalDictionaryList) {
                DocumentContext documentContext = JsonPath.parse(extDictionaryItem);
                String labelValue = processDictionaryRefValue(labelPath, component, scenarioDto, documentContext);
                String codeValue = processDictionaryRefValue(codePath, component, scenarioDto, documentContext);
                if (StringUtils.isEmpty(codeValue) || StringUtils.isEmpty(labelValue)) {
                    log.error("error get label({}) and code({}) from object: {}",  labelPath, codePath, documentContext);
                    throw new FormBaseException(String.format("Error processing object from json: {}", documentContext));
                }
                items.add(Map.of("label",labelValue,"code", codeValue, "value", extDictionaryItem));
            }
            component.getAttrs().put(DICTIONARY_LIST_KEY, items);
        } catch (JsonProcessingException e) {
            throw new JsonParsingException("Error while processing object from json: dictionaryRef" , e);
        }
    }

    private String processDictionaryRefValue(String labelPath, FieldComponent component, ScenarioDto scenarioDto, DocumentContext documentContext) {
        String value = componentReferenceService.getValueByContext(labelPath, Function.identity(),
                componentReferenceService.buildPlaceholderContext(component, scenarioDto),
                documentContext);
        if (labelPath.equals(value)) {
            value = jsonProcessingService.getFieldFromContext(labelPath, documentContext, String.class);
        }
        return value;
    }
}
