package ru.gosuslugi.pgu.fs.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.core.exception.JsonParsingException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.components.ComponentAttributes;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.fs.common.jsonlogic.JsonLogic;
import ru.gosuslugi.pgu.fs.common.service.ComponentReferenceService;
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.DictionaryFilter;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.MapServiceAttributes;
import ru.gosuslugi.pgu.fs.utils.AttributeValueTypes;
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilter;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterRequest;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterSimple;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryUnionType;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiSimpleDictionaryFilterContainer;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiUnionDictionaryFilterContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ATTRIBUTE_TYPE;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ATTRIBUTE_VALUES;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DICTIONARY_FILTER_IN_REF;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DICTIONARY_FILTER_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DICTIONARY_FILTER_RELATION;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DICTIONARY_FILTER_RELATION_FILTER_ON;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DICTIONARY_FILTER_VALUE_TYPE_PRESET;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DICTIONARY_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DICTIONARY_OPTIONS;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DICT_FILTER_ATTRIBUTE_NAME;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DICT_FILTER_VALUE_NAME;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DICT_FILTER_VALUE_TYPE;
import static ru.gosuslugi.pgu.components.ComponentAttributes.NOT_CORRECT_FILTER_VALUE_TYPE;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ORIGINAL_ITEM;
import static ru.gosuslugi.pgu.components.ComponentAttributes.REF_ATTR;

@Slf4j
@Component
@RequiredArgsConstructor
public class NsiDictionaryFilterHelper {

    public static final String NOT_CORRECT_JSON_FORMAT = "Ошибка при формировании условий для проверки корректности выбранного значения";
    public static final String NOT_CORRECT_FILTER_CONDITION_DESCRIPTION = "Неверный формат описания условия фильтра";
    // Атрибут содержащий тип фильтрации
    public static final String DICT_FILTER_CONDITION = "condition";
    private static final String DICT_PARENT_REF_ITEM_VALUE = "parentRefItemValue";
    private static final String DICT_VALIDATE_DICTIONARY_SIZE = "validateDictionarySize";

    private final ParseAttrValuesHelper parseAttrValuesHelper;
    private final ComponentReferenceService componentReferenceService;

    private final JsonProcessingService jsonProcessingService;

    /**
     * Создаёт запрос, который будет передан в дальнейшем в nsi-сервис.
     * При наличии в компоненте атрибута {@link ComponentAttributes#DICTIONARY_FILTER_NAME_ATTR}
     * идёт дополнительное создание условий и добавление их в создаваемый запрос.
     * Используется в KindergartenMapServiceComponent
     *
     * @param scenarioDto      сценарий
     * @param fieldComponent   компонент
     * @param presetProperties предустановленные свойства для фильтра
     * @return запрос к nsi-сервису
     */
    public NsiDictionaryFilterRequest buildNsiDictionaryFilterRequest(ScenarioDto scenarioDto,
                                                                      FieldComponent fieldComponent,
                                                                      Map<String, String> presetProperties) {
        List<NsiDictionaryFilterSimple.Builder> conditionBuilders = new ArrayList<>();
        try {
            if (fieldComponent.getAttrs().containsKey(DICTIONARY_FILTER_NAME_ATTR)) {
                @SuppressWarnings("unchecked") var filters = (List<Map<String, Object>>) fieldComponent.getAttrs().get(DICTIONARY_FILTER_NAME_ATTR);
                for (var filter : filters) {
                    if (!filter.containsKey(DICT_FILTER_VALUE_TYPE)) {
                        throw new FormBaseException(NOT_CORRECT_FILTER_CONDITION_DESCRIPTION);
                    }
                    NsiDictionaryFilterSimple.Builder filterValueBuilder = getSimpleConditionBuilder(scenarioDto, filter, presetProperties);
                    conditionBuilders.add(filterValueBuilder);
                }
            }
            if (fieldComponent.getAttrs().containsKey(REF_ATTR) &&
                    JsonLogic.isTrue(fieldComponent.getAttrs().get(DICTIONARY_FILTER_IN_REF))) {
                var filtersList = getDictionaryFiltersListFromRef(fieldComponent);
                for (var filter : filtersList) {
                    if (filter.get(DICT_FILTER_VALUE_TYPE).equals(DICTIONARY_FILTER_VALUE_TYPE_PRESET)) {
                        if (presetProperties.containsKey(filter.get(DICT_FILTER_ATTRIBUTE_NAME))) {
                            filter.put(DICT_FILTER_VALUE_NAME, filter.get(DICT_FILTER_ATTRIBUTE_NAME));
                        }
                    }
                    NsiDictionaryFilterSimple.Builder filterValueBuilder = getSimpleConditionBuilder(scenarioDto, filter, presetProperties);
                    conditionBuilders.add(filterValueBuilder);
                }
            }
        } catch (JSONException e) {
            throw new JsonParsingException(NOT_CORRECT_JSON_FORMAT, e);
        }
        NsiDictionaryFilter filter = null;
        if (conditionBuilders.size() == 1) {
            NsiSimpleDictionaryFilterContainer container = new NsiSimpleDictionaryFilterContainer();
            container.setSimple(conditionBuilders.get(0).build());
            filter = container;
        }
        if (conditionBuilders.size() > 1) {
            filter = new NsiUnionDictionaryFilterContainer.Builder()
                    .setFilterBuilders(conditionBuilders)
                    .setNsiDictionaryUnionType(NsiDictionaryUnionType.AND)
                    .build();
        }

        Map<?, ?> dictionaryOptions = null;
        var dictOptions = fieldComponent.getAttrs().get(DICTIONARY_OPTIONS);
        if (dictOptions instanceof Map) {
            dictionaryOptions = (Map<?, ?>) dictOptions;
        }

        String parentRefItemValue = null;
        if (!CollectionUtils.isEmpty(dictionaryOptions)) {
            parentRefItemValue = String.valueOf(dictionaryOptions.get(DICT_PARENT_REF_ITEM_VALUE));
        }

        String pageSize = Objects.toString(fieldComponent.getAttrs().get(DICT_VALIDATE_DICTIONARY_SIZE), "1000");
        return new NsiDictionaryFilterRequest.Builder()
                .setPageNum("1")
                .setPageSize(pageSize)
                .setFilter(filter)
                .setParentRefItemValue(parentRefItemValue)
                .build();
    }

    /**
     * Создаёт запрос для передачи в nsi-сервис.
     * Используется в MapServiceComponent
     */
    public NsiDictionaryFilterRequest buildNsiDictionaryFilterRequest(ScenarioDto scenarioDto,
                                                                      MapServiceAttributes attributes,
                                                                      Map<String, String> presetProperties,
                                                                      List<NsiDictionaryFilterSimple.Builder> conditionBuilders) {
        try {
            List<DictionaryFilter> dictionaryFilters = attributes.getDictionaryFilters();
            if (nonNull(dictionaryFilters)) {
                for (DictionaryFilter simpleFilter : dictionaryFilters) {
                    if (isNull(simpleFilter.getValueType())) {
                        throw new FormBaseException(NOT_CORRECT_FILTER_CONDITION_DESCRIPTION);
                    }
                    Map<String, Object> filter = new HashMap<>() {{
                        put(ComponentAttributes.DICT_FILTER_VALUE_TYPE, simpleFilter.getValueType());
                        put(DICT_FILTER_VALUE_TYPE, simpleFilter.getValueType());
                        put(ComponentAttributes.DICT_FILTER_ATTRIBUTE_NAME, simpleFilter.getAttributeName());
                        put(DICT_FILTER_CONDITION, simpleFilter.getCondition());
                        put(ComponentAttributes.DICT_FILTER_VALUE_NAME, simpleFilter.getValue());
                    }};
                    NsiDictionaryFilterSimple.Builder filterValueBuilder = getSimpleConditionBuilder(scenarioDto, filter, presetProperties);
                    conditionBuilders.add(filterValueBuilder);
                }
            }
        } catch (JSONException e) {
            throw new JsonParsingException(NOT_CORRECT_JSON_FORMAT, e);
        }
        NsiUnionDictionaryFilterContainer filter = new NsiUnionDictionaryFilterContainer.Builder()
                .setFilterBuilders(conditionBuilders)
                .setNsiDictionaryUnionType(NsiDictionaryUnionType.AND)
                .build();
        return new NsiDictionaryFilterRequest.Builder()
                .setPageNum("1")
                .setPageSize("1")
                .setFilter(filter)
                .build();
    }

    public Map<String, String> getPresetValue(FieldComponent component, Supplier<ComponentResponse<String>> supplier) {
        String presetData = component.getValue();
        if (!StringUtils.hasText(presetData)) {
            presetData = supplier.get().get();
        }
        Map<String, String> presetProperties = null;
        if (Objects.nonNull(presetData)) {
            presetProperties = JsonProcessingUtil.fromJson(presetData, new TypeReference<>() {
            });
            presetProperties = JsonProcessingUtil.fromJson(presetData, new TypeReference<>() {
            });
        }
        return presetProperties;
    }

    /**
     * Получение объекта-билдера простого условия фильтрации для справочника массива описаний фильтра
     *
     * @param scenarioDto      объект ДТО полного сценария
     * @param simpleFilter     карта с описанием атрибутов одного простого условия фильтрации
     * @param presetProperties значения предзаполненных полей, для формирования фильтров
     * @return объект-билдер для формирования простого условия фильтрации
     * @throws JSONException исключение в случае ошибки парсинга json
     */
    public NsiDictionaryFilterSimple.Builder getSimpleConditionBuilder(ScenarioDto scenarioDto,
                                                                       Map<String, Object> simpleFilter,
                                                                       Map<String, String> presetProperties) throws JSONException {
        String valueType = simpleFilter.get(DICT_FILTER_VALUE_TYPE).toString();
        if (Objects.isNull(valueType))
            throw new FormBaseException(String.format(NOT_CORRECT_FILTER_VALUE_TYPE, valueType));
        NsiDictionaryFilterSimple.Builder filterValueBuilder =
                new NsiDictionaryFilterSimple.Builder()
                        .setAttributeName(simpleFilter.get(DICT_FILTER_ATTRIBUTE_NAME).toString())
                        .setCondition(simpleFilter.get(DICT_FILTER_CONDITION).toString());

        String presetAttrNameVal = simpleFilter.get(DICT_FILTER_VALUE_NAME).toString();
        if (Objects.nonNull(presetAttrNameVal) && !presetAttrNameVal.isBlank()) {
            String attributeType = Optional.ofNullable(simpleFilter.get(ATTRIBUTE_TYPE)).map(Object::toString).orElse(null);
            String presetValue = getAttrValueByType(valueType, attributeType, presetAttrNameVal, scenarioDto, presetProperties);
            filterValueBuilder.setValue(attributeType, presetValue);
        } else if (presetProperties.containsKey(simpleFilter.get(DICT_FILTER_ATTRIBUTE_NAME).toString())) {
            filterValueBuilder.setStringValue(presetProperties.get(simpleFilter.get(DICT_FILTER_ATTRIBUTE_NAME).toString()));
        }
        return filterValueBuilder;
    }

    /**
     * Вычисляет по типу атрибута {@link AttributeValueTypes} их значения.
     *
     * @param valueType         тип атрибута из {@link AttributeValueTypes}, можно передавать в любом регистре
     * @param attributeType     тип значения атрибута
     * @param presetAttrNameVal значение из атрибута {@link ComponentAttributes#DICT_FILTER_VALUE_NAME}
     *                          внутри фильтра {@link ComponentAttributes#DICTIONARY_FILTER_NAME_ATTR}
     * @param scenarioDto       сценарий
     * @param presetProperties  значения предзаполненных полей, для формировани фильтров
     * @return вычисленное значение атрибута
     * @throws JSONException исключение в случае ошибки парсинга json
     */
    private String getAttrValueByType(String valueType,
                                      String attributeType,
                                      String presetAttrNameVal,
                                      ScenarioDto scenarioDto,
                                      Map<String, String> presetProperties) throws JSONException {
        AttributeValueTypes type;
        try {
            type = AttributeValueTypes.valueOf(valueType.toUpperCase());
        } catch (IllegalArgumentException ex) {
            String errorMessage = String.format(NOT_CORRECT_FILTER_VALUE_TYPE, valueType);
            log.error(errorMessage, ex);
            throw new FormBaseException(errorMessage);
        }
        return type.getPresetValue(parseAttrValuesHelper, valueType, attributeType, presetAttrNameVal, scenarioDto, presetProperties);
    }

    /**
     * Достает dictionaryFilter из ref
     *
     * @param fieldComponent компонент
     * @return список фильтров описанных в ref
     */
    public List<Map<String, Object>> getDictionaryFiltersListFromRef(FieldComponent fieldComponent) {
        @SuppressWarnings("unchecked") var refs = (List<Map<String, Object>>) fieldComponent.getAttrs().get(REF_ATTR);
        for (var ref : refs) {
            if (ref.containsKey(DICTIONARY_FILTER_RELATION) && ref.get(DICTIONARY_FILTER_RELATION).equals(DICTIONARY_FILTER_RELATION_FILTER_ON)) {
                return (List<Map<String, Object>>) ref.get(DICTIONARY_FILTER_NAME_ATTR);
            }
        }
        return refs;
    }

    /**
     * Подготовливает основной процесс сборки последующего запроса в справочник,
     * устанавливаем свойства для фильтра, рассчитаных на FE
     *
     * @param fieldComponent компонент
     * @param scenarioDto    сценарий
     * @return запрос к сервису
     */
    public NsiDictionaryFilterRequest buildDictionaryFilterRequestFromRef(ScenarioDto scenarioDto, FieldComponent fieldComponent) {

        var refs = (List<Map<String, Object>>) fieldComponent.getAttrs().get(REF_ATTR);
        String relatedRel = "";
        for (var ref : refs) {
            if (ref.containsKey(DICTIONARY_FILTER_RELATION) && ref.get(DICTIONARY_FILTER_RELATION).equals(DICTIONARY_FILTER_RELATION_FILTER_ON)) {
                relatedRel = (String) ref.get("relatedRel");
            }
        }
        var relatedLookupName = scenarioDto.getCurrentValue().get(relatedRel);
        Map<String, Object> relatedLookupMap = jsonProcessingService.fromJson
                (AnswerUtil.getValue(AnswerUtil.createAnswerEntry(relatedRel, relatedLookupName.getValue())),
                        new TypeReference<>() {});

        String finalRelatedRel = relatedRel;
        Optional<FieldComponent> optionalRelatedComponent =
                scenarioDto
                        .getDisplay()
                        .getComponents()
                        .stream()
                        .filter(el -> el.getId().equals(finalRelatedRel))
                        .findFirst();

        String calculatedType = (String) ((optionalRelatedComponent.isPresent())
                ? optionalRelatedComponent.get().getAttrs().get(DICTIONARY_NAME_ATTR) : " ");

        Map<String, Object> originalItem2 = (Map<String, Object>) relatedLookupMap.get(ORIGINAL_ITEM);
        Map<String, String> attributeValues2 = (Map<String, String>) originalItem2.get(ATTRIBUTE_VALUES);
        attributeValues2.put(("FIAS_" + calculatedType.toUpperCase()), (String) originalItem2.get("value"));

        return buildNsiDictionaryFilterRequest(scenarioDto, fieldComponent, attributeValues2);
    }
}