package ru.gosuslugi.pgu.fs.component.dictionary;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.common.core.exception.JsonParsingException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.components.ComponentAttributes;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.ComponentResponse;
import ru.gosuslugi.pgu.fs.common.utils.PguAuthHeadersUtil;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.MapServiceAttributes;
import ru.gosuslugi.pgu.fs.service.DictionaryFilterService;
import ru.gosuslugi.pgu.fs.service.LkNotifierService;
import ru.gosuslugi.pgu.fs.service.impl.NsiDictionaryFilterHelper;
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper;
import ru.gosuslugi.pgu.fs.utils.ContextJsonUtil;
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionary;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterRequest;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterSimple;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiFilterCondition;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDadataService;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import static java.util.Objects.nonNull;

/**
 * Компонент выбора подразделения ведомства на карте.
 */
@Slf4j
@Component
public class MapServiceComponent extends AbstractMapComponent<String> implements AttributesDto<MapServiceAttributes> {

    private static final String CURRENT_VALUE_ATTRIBUTE_NAME = "value";
    private static final String CHECK_VALUE_ATTRIBUTE_NAME = "value";
    private static final String WITH_TOKEN = "withToken";

    public MapServiceComponent(LkNotifierService lkNotifierService,
                               CalculatedAttributesHelper calculatedAttributesHelper,
                               ParseAttrValuesHelper parseAttrValuesHelper,
                               NsiDadataService nsiDadataService,
                               UserPersonalData userPersonalData,
                               DictionaryFilterService dictionaryFilterService,
                               NsiDictionaryService nsiDictionaryService,
                               NsiDictionaryFilterHelper nsiDictionaryFilterHelper) {
        super(lkNotifierService, calculatedAttributesHelper, parseAttrValuesHelper, nsiDadataService,
                userPersonalData, dictionaryFilterService, nsiDictionaryService, nsiDictionaryFilterHelper);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.MapService;
    }

    @Override
    public TypeReference<MapServiceAttributes> getAttributesDtoType() {
        return new TypeReference<>() {};
    }

    @Override
    public ComponentResponse<String> getInitialValue(FieldComponent component, ScenarioDto scenarioDto) {
        MapServiceAttributes attrsDto = getAttributesDto(component);
        Map<String, Object> initialValue = dictionaryFilterService.getInitialValue(component, scenarioDto);
        initialValue.putAll(getAddressData(component, scenarioDto, attrsDto));
        initialValue.putAll(getPersonalDataValues(scenarioDto, attrsDto));
        return ComponentResponse.of(jsonProcessingService.toJson(initialValue));
    }

    @Override
    public void preloadComponent(FieldComponent component, ScenarioDto scenarioDto) {
        dictionaryFilterService.preloadComponent(component, scenarioDto, () -> getInitialValue(component, scenarioDto));
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry,
                                       ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        MapServiceAttributes attributes = getAttributesDto(fieldComponent);
        if (!attributes.isValidationOn()) {
            return;
        }
        try {
            NsiDictionary dictionary = getDictionaryForFilter(attributes, scenarioDto, fieldComponent, entry);
            if (NSI_DICTIONARY_EMPTY_PREDICATE.test(dictionary)) {
                if (attributes.useSecondFilter()) {
                    dictionary = getDictionaryForFilter(attributes, scenarioDto, fieldComponent, entry);
                }
                if (NSI_DICTIONARY_EMPTY_PREDICATE.test(dictionary)) {
                    incorrectAnswers.put(entry.getKey(),
                            String.format(ComponentAttributes.VALUE_NOT_FOUND_MESSAGE, entry.getValue().getValue()));
                }
            }
        } catch (JSONException ex) {
            throw new JsonParsingException(NOT_CORRECT_JSON_FORMAT, ex);
        }
    }

    private NsiDictionary getDictionaryForFilter(MapServiceAttributes mapServiceAttributes,
                                                 ScenarioDto scenarioDto,
                                                 FieldComponent fieldComponent,
                                                 Entry<String, ApplicantAnswer> entry) throws JSONException {
        String presetData = getInitialValue(fieldComponent, scenarioDto).get();
        Map<String, String> presetProperties = null;
        if (nonNull(presetData)) {
            presetProperties = JsonProcessingUtil.fromJson(presetData, new TypeReference<>() {});
        }
        String dictionaryName = mapServiceAttributes.getDictionaryType();
        List<NsiDictionaryFilterSimple.Builder> conditionBuilders = new ArrayList<>();

        if (mapServiceAttributes.getDictionaryFilters().stream().anyMatch(el -> CURRENT_VALUE_ATTRIBUTE_NAME.equals(el.getAttributeName()))) {
            Entry<String, String> entryInDepth = ContextJsonUtil.getAttributeInDepth(jsonProcessingService, entry, null, CURRENT_VALUE_ATTRIBUTE_NAME);
            String selectedValue = entryInDepth.getValue();
            NsiDictionaryFilterSimple.Builder selectedFilterValueBuilder = new NsiDictionaryFilterSimple.Builder()
                    .setAttributeName(CHECK_VALUE_ATTRIBUTE_NAME)
                    .setCondition(NsiFilterCondition.EQUALS.toString())
                    .setStringValue(selectedValue);
            conditionBuilders.add(selectedFilterValueBuilder);
        }
        NsiDictionaryFilterRequest requestBody =
                nsiDictionaryFilterHelper.buildNsiDictionaryFilterRequest(scenarioDto, mapServiceAttributes, presetProperties, conditionBuilders);

        String booleanStringValue = Objects.toString(fieldComponent.getAttrs().get(WITH_TOKEN), "false");
        if (Boolean.parseBoolean(booleanStringValue)) {
            HttpHeaders headers = PguAuthHeadersUtil.prepareAuthCookieHeaders(personalData.getToken());
            return nsiDictionaryService.getDictionaryItemForMapsByFilter(dictionaryName, requestBody, headers);
        }
        return nsiDictionaryService.getDictionaryItemForMapsByFilter(dictionaryName, requestBody);
    }
}
