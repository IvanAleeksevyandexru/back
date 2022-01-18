package ru.gosuslugi.pgu.fs.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.components.ComponentAttributes;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.DictionaryFilters;
import ru.gosuslugi.pgu.fs.service.DictionaryFilterService;
import ru.gosuslugi.pgu.fs.service.validation.DictionaryFilterValidationStrategy;
import ru.gosuslugi.pgu.fs.service.validation.impl.DictType;
import ru.gosuslugi.pgu.fs.utils.AttributeValueTypes;
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterRequest;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static ru.gosuslugi.pgu.components.ComponentAttributes.DICTIONARY_FILTERS_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DICTIONARY_FILTER_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DICTIONARY_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DICT_FILTER_ATTRIBUTE_NAME;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DICT_FILTER_VALUE_NAME;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DICT_FILTER_VALUE_TYPE;
import static ru.gosuslugi.pgu.components.ComponentAttributes.DICT_URL_TYPE;
import static ru.gosuslugi.pgu.components.ComponentAttributes.SECONDARY_DICTIONARY_FILTER_NAME_ATTR;

@Slf4j
@Service
@RequiredArgsConstructor
public class DictionaryFilterServiceImpl implements DictionaryFilterService {

    private final NsiDictionaryService nsiDictionaryService;
    private final NsiDictionaryFilterHelper nsiDictionaryFilterHelper;
    private final CalculatedAttributesHelper calculatedAttributesHelper;
    private final List<DictionaryFilterValidationStrategy> dictionaryValidationStrategies;

    @Override
    public Map<String, Object> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        Map<String, Object> presetValues = new HashMap<>();
        presetValues.putAll(calculatedAttributesHelper.getAllCalculatedValues(DICTIONARY_FILTER_NAME_ATTR, component, scenarioDto));
        presetValues.putAll(calculatedAttributesHelper.getAllCalculatedValues(SECONDARY_DICTIONARY_FILTER_NAME_ATTR, component, scenarioDto));
        presetDictionaryFilters(component, scenarioDto, presetValues);
        presetValues.putAll(getServiceInfoPresetValues(component, scenarioDto));
        return presetValues;
    }

    @Override
    public void preloadComponent(FieldComponent component, ScenarioDto scenarioDto, Supplier<ComponentResponse<String>> supplier) {
        Map<String, String> presetProperties = nsiDictionaryFilterHelper.getPresetValue(component, supplier);
        String dictionaryName = component.getAttrs().get(DICTIONARY_NAME_ATTR).toString();
        NsiDictionaryFilterRequest requestBody = nsiDictionaryFilterHelper.buildNsiDictionaryFilterRequest(scenarioDto, component, presetProperties);
        nsiDictionaryService.asyncGetDictionaryItemForMapsByFilter(dictionaryName, requestBody);
    }

    @Override
    public void validateAfterSubmit(Map<String, String> incorrectAnswers,
                                    Map.Entry<String, ApplicantAnswer> entry,
                                    ScenarioDto scenarioDto,
                                    FieldComponent fieldComponent,
                                    Supplier<ComponentResponse<String>> supplier) {
        String dictType = String.valueOf(fieldComponent.getAttrs().get(DICT_URL_TYPE));
        DictionaryFilterValidationStrategy validationStrategy = getValidationStrategy(DictType.fromStringOrDefault(dictType, DictType.dictionary));
        validationStrategy.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent, supplier);
    }

    private void presetDictionaryFilters(FieldComponent component, ScenarioDto scenarioDto, Map<String, Object> presetValues) {
        var dictionaryFilters = component.getAttrs().get(DICTIONARY_FILTERS_NAME_ATTR);

        if (Objects.nonNull(dictionaryFilters)) {
            DictionaryFilters dictionaryFiltersDto = new DictionaryFilters();
            dictionaryFiltersDto.setFilters(JsonProcessingUtil.fromJson(JsonProcessingUtil.toJson(dictionaryFilters), new TypeReference<>() {}));

            if (!CollectionUtils.isEmpty(dictionaryFiltersDto.getFilters())) {
                var filters = new LinkedList<>();
                dictionaryFiltersDto.getFilters().forEach(filter -> filters.add(calculatedAttributesHelper.getAllCalculatedValues(filter, component, scenarioDto)));
                presetValues.put(DICTIONARY_FILTERS_NAME_ATTR, filters);
            }
        }
    }

    /**
     * Возвращает мапу из атрибутов типа {@link AttributeValueTypes#SERVICEINFO}
     *
     * @param component   компонент, в атрибутах которого есть {@link ComponentAttributes#DICTIONARY_FILTER_NAME_ATTR}
     * @param scenarioDto сценарий
     * @return мапа из атрибутов типа {@link AttributeValueTypes#SERVICEINFO}
     */
    private Map<String, Object> getServiceInfoPresetValues(FieldComponent component, ScenarioDto scenarioDto) {
        Map<String, Object> result = new HashMap<>();
        if (Objects.nonNull(component.getAttrs()) && component.getAttrs().containsKey(DICTIONARY_FILTER_NAME_ATTR)) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> filters = (ArrayList<Map<String, Object>>) component.getAttrs().get(DICTIONARY_FILTER_NAME_ATTR);
            DocumentContext serviceInfoContext = JsonPath.parse(JsonProcessingUtil.toJson(scenarioDto.getServiceInfo()));
            Predicate<Map<String, Object>> serviceInfoPredicate = filter ->
                    Objects.nonNull(filter.get(DICT_FILTER_VALUE_TYPE))
                            && AttributeValueTypes.SERVICEINFO.toString().equalsIgnoreCase(filter.get(DICT_FILTER_VALUE_TYPE).toString());
            filters.stream()
                    .filter(serviceInfoPredicate)
                    .forEach(filter -> {
                        Object value;
                        try {
                            value = serviceInfoContext.read("$." + filter.get(DICT_FILTER_VALUE_NAME));
                        } catch (PathNotFoundException e) {
                            value = null;
                        }
                        String attrName = filter.get(DICT_FILTER_ATTRIBUTE_NAME).toString();
                        result.put(attrName, value);
                        filter.put(DICT_FILTER_VALUE_NAME, attrName);
                    });
        }
        return result;
    }

    private DictionaryFilterValidationStrategy getValidationStrategy(DictType type) {
        return dictionaryValidationStrategies.stream()
                .filter(strategy -> strategy.getDictUrlType().equals(type))
                .findFirst()
                .orElseThrow();
    }
}
