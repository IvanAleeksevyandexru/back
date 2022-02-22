package ru.gosuslugi.pgu.fs.utils;

import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.service.ProtectedFieldService;
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry;
import ru.gosuslugi.pgu.fs.common.variable.VariableType;

import java.util.Map;
import java.util.Objects;

import static java.util.Objects.isNull;

/**
 * Класс-хелпер для поиска значений, указанных в качестве референсов для получения значений компонентов
 * предыдущих экранов
 */
@Component
@RequiredArgsConstructor
public class ParseAttrValuesHelper {

    private static final String TYPE_ATTRIBUTE_NAME = "type";
    private static final String VALUE_ATTRIBUTE_NAME = "value";
    private static final String ARRAY_CALCULATION_REG_EXP = "[A-Za-z]+\\[[0-9]\\]";

    private final VariableRegistry variableRegistry;
    private final JsonProcessingService jsonProcessingService;
    private final ProtectedFieldService protectedFieldService;

    enum AttrTypesEnum {
        CONST,
        REF,
        VAR
    }

    /**
     * Получить значение атрибута по переданному пути в истории в формате строки
     * в цепочке json объектов
     *
     * @param refAttr     путь к искомому значению
     * @param scenarioDto параметры запроса на входе
     * @return значение атрибута в формате строки
     */
    public String getStringValueOfRefAttribute(String refAttr, ScenarioDto scenarioDto) {
        String returnValue = "";
        if (Objects.isNull(refAttr)) return returnValue;
        String[] pathElements = refAttr.split("[.]");
        String appFieldVal = getPrevScreenFieldData(pathElements[0], scenarioDto);
        if (Objects.isNull(appFieldVal) || appFieldVal.isEmpty()) return returnValue;
        if (pathElements.length == 1) return appFieldVal;
        // В промежутке JSONObject
        try {
            JSONObject subLevelObj = new JSONObject(appFieldVal);
            for (int i = 1; i < pathElements.length - 1; i++) {
                subLevelObj = subLevelObj.getJSONObject(pathElements[i]);
            }
            // curInd length - 1 - вернуть как стринг
            return subLevelObj.get(pathElements[pathElements.length - 1]).toString();
        } catch (JSONException e) {
            // пока в случае ошибки разбора пути - игнорируем
        }
        return returnValue;
    }

    /**
     * Получить значение атрибута по переданному пути в истории в формате строки
     * в цепочке json объектов
     *
     * @param refAttr     путь к искомому значению
     * @param scenarioDto параметры запроса на входе
     * @return значение атрибута в формате строки
     */
    public String getStringValueOfRefAttributeByFullPath(String refAttr, ScenarioDto scenarioDto) {
        String returnValue = "";
        if (Objects.isNull(refAttr)) return returnValue;
        String[] pathElements = refAttr.split("[.]");

        if (pathElements.length == 1)
            return getPrevScreenFieldFullData(pathElements[0], scenarioDto);


        String appFieldVal = getPrevScreenFieldData(pathElements[0], scenarioDto);
        if (Objects.isNull(appFieldVal) || appFieldVal.isEmpty()) return returnValue;
        if (pathElements.length == 2) return appFieldVal;
        // В промежутке JSONObject
        try {
            JSONObject subLevelObj = new JSONObject(appFieldVal);
            for (int i = 2; i < pathElements.length - 1; i++) {
                val pathElement = pathElements[i];
                if (StringUtils.isNotBlank(pathElement) && pathElement.matches(ARRAY_CALCULATION_REG_EXP)) {
                    val arrayName = pathElement.substring(0, pathElement.indexOf("["));
                    val array = subLevelObj.getJSONArray(arrayName);
                    val index = Integer.valueOf(StringUtils.substringBetween(pathElement, "[", "]"));
                    subLevelObj = array.getJSONObject(index);
                } else {
                    subLevelObj = subLevelObj.getJSONObject(pathElement);
                }
            }
            // curInd length - 1 - вернуть как стринг
            return subLevelObj.get(pathElements[pathElements.length - 1]).toString();
        } catch (JSONException e) {
            // пока в случае ошибки разбора пути - игнорируем
        }
        return returnValue;
    }

    /**
     * Получить сохраненное значение для компонента из истории
     *
     * @param applicationFieldId идентификатор искомого applicationFieldId
     * @param scenarioDto        параметры запроса
     * @return значение в формате строки
     */
    public String getPrevScreenFieldData(String applicationFieldId, ScenarioDto scenarioDto) {
        if (scenarioDto.getCurrentValue().containsKey(applicationFieldId)) {
            // Сначала ищем в списке ответов предыдущего экрана
            return scenarioDto.getCurrentValue().get(applicationFieldId).getValue();
        }
        if (scenarioDto.getApplicantAnswers().containsKey(applicationFieldId)) {
            return scenarioDto.getApplicantAnswers().get(applicationFieldId).getValue();
        }
        return null;
    }

    /**
     * Получить сохраненное значение для компонента из истории
     *
     * @param applicationFieldId идентификатор искомого applicationFieldId
     * @param scenarioDto        параметры запроса
     * @return значение в формате строки
     */
    public String getPrevScreenFieldFullData(String applicationFieldId, ScenarioDto scenarioDto) {
        if (scenarioDto.getCurrentValue().containsKey(applicationFieldId)) {
            // Сначала ищем в списке ответов предыдущего экрана
            return JsonProcessingUtil.toJson(scenarioDto.getCurrentValue());
        }
        if (scenarioDto.getApplicantAnswers().containsKey(applicationFieldId)) {
            return scenarioDto.getApplicantAnswers().get(applicationFieldId).getValue();
        }
        return null;
    }


    /**
     * Получить описание кастомного атрибута содержащего тип значения атрибута и его значение
     *
     * @param customAttribute map свойств объекта кастомного атрибута
     * @param scenarioDto     описание текущего шага сценария
     * @return
     */
    public String getAttributeValue(Map<String, String> customAttribute, ScenarioDto scenarioDto) {
        String attributeType = customAttribute.get(TYPE_ATTRIBUTE_NAME);
        if (Objects.nonNull(attributeType)) {
            if (AttrTypesEnum.REF.name().equalsIgnoreCase(attributeType)) {
                String refValue = customAttribute.get(VALUE_ATTRIBUTE_NAME);
                if (Objects.nonNull(refValue)) {
                    return getStringValueOfRefAttribute(refValue, scenarioDto);
                }
            }
            if (AttrTypesEnum.VAR.name().equalsIgnoreCase(attributeType)) {
                String varValue = customAttribute.get(VALUE_ATTRIBUTE_NAME);
                if (Objects.nonNull(varValue)) {
                    return variableRegistry.getVariable(VariableType.valueOf(varValue)).getValue(scenarioDto);
                }
            }
            return customAttribute.get(VALUE_ATTRIBUTE_NAME);
        }
        return null;
    }

    /**
     * Получить описание кастомного атрибута содержащего тип значения атрибута и его значение
     *
     * @param customAttribute map свойств объекта кастомного атрибута
     * @param scenarioDto     описание текущего шага сценария
     * @return
     */
    public String getAttributeValueByDocumentContext(Map<String, String> customAttribute, ScenarioDto scenarioDto) {
        String attributeType = customAttribute.get(TYPE_ATTRIBUTE_NAME);
        if (Objects.nonNull(attributeType)) {
            if (AttrTypesEnum.REF.name().equalsIgnoreCase(attributeType)) {
                String refValue = customAttribute.get(VALUE_ATTRIBUTE_NAME);
                if (Objects.nonNull(refValue)) {
                    String key = refValue.replace("$", "");
                    String result = null;
                    try {
                        result = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getCurrentValue())).read(key, String.class);
                    } catch (Exception e) {
                        // Nothing
                    }
                    if (isNull(result)) {
                        try {
                            result = JsonPath.parse(jsonProcessingService.convertAnswersToJsonString(scenarioDto.getApplicantAnswers())).read(key, String.class);
                        } catch (Exception e) {
                            // Nothing
                        }
                    }
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * Получить значение защищенного поля по имени
     *
     * @param fieldName имя защищенного поля
     * @return значение защищенного поля
     */
    public String getProtectedField(String fieldName) {
        return Objects.toString(protectedFieldService.getValue(fieldName), null);
    }

}
