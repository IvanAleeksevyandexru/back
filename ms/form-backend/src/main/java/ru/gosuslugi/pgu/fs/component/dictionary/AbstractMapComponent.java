package ru.gosuslugi.pgu.fs.component.dictionary;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.AttrValue;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.MapServiceAttributes;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.MvdFilter;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.PresetField;
import ru.gosuslugi.pgu.fs.service.DictionaryFilterService;
import ru.gosuslugi.pgu.fs.service.LkNotifierService;
import ru.gosuslugi.pgu.fs.service.impl.NsiDictionaryFilterHelper;
import ru.gosuslugi.pgu.fs.utils.CalculatedAttributesHelper;
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddress;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressElement;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.DadataAddressResponse;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionary;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDadataService;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.Collections.emptySet;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.springframework.util.StringUtils.hasText;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ADDRESS;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ADDRESS_FIAS;
import static ru.gosuslugi.pgu.components.ComponentAttributes.FIRST_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.GEO_LAT_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.GEO_LON_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.LAST_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.MIDDLE_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.MVD_SOURCE_ATTR_NAME;
import static ru.gosuslugi.pgu.components.ComponentAttributes.OKATO_ATTR_NAME;
import static ru.gosuslugi.pgu.components.ComponentAttributes.OKTMO_TERRITORY_11_ATTR_NAME;
import static ru.gosuslugi.pgu.components.ComponentAttributes.OKTMO_TERRITORY_8_ATTR_NAME;
import static ru.gosuslugi.pgu.components.ComponentAttributes.PREV_STEP_CLEAN_CACHE_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.REG_CODE_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.REG_OKATO_ATTR;

@Slf4j
public abstract class AbstractMapComponent<PreSetModel> extends AbstractComponent<PreSetModel> {

    public static final int FIAS_REGION_LEVEL_CODE = 1;

    // Список простых условий фильтра с правилами получения значений для выбора данных из справочника
    private static final String USER_ID_FIELD_NAME = "userId";
    private static final String ORDER_ID_FIELD_NAME = "orderId";

    // Значение атрибута
    private static final String STORED_VALUES_ATTRIBUTE_NAME = "storedValues";

    protected static final String NOT_CORRECT_JSON_FORMAT = "Ошибка при формировании условий для проверки корректности выбранного значения";

    // Предикаты адреса
    private static final Predicate<DadataAddressElement> IS_FIRST_LEVEL_ELEMENT_PREDICATE = (address) -> address.getLevel() == 1;
    private static final Predicate<DadataAddressElement> IS_FOUR_LEVEL_ELEMENT_PREDICATE = (address) -> address.getLevel() == 4;

    // Предикат словаря
    protected static final Predicate<NsiDictionary> NSI_DICTIONARY_EMPTY_PREDICATE =
            (dictionary) -> StringUtils.isEmpty(dictionary.getTotal()) || "0".equals(dictionary.getTotal());

    private final LkNotifierService lkNotifierService;
    private final CalculatedAttributesHelper calculatedAttributesHelper;
    private final ParseAttrValuesHelper parseAttrValuesHelper;
    private final NsiDadataService nsiDadataService;
    protected final UserPersonalData personalData;
    protected final DictionaryFilterService dictionaryFilterService;
    protected final NsiDictionaryFilterHelper nsiDictionaryFilterHelper;
    protected final NsiDictionaryService nsiDictionaryService;

    protected AbstractMapComponent(LkNotifierService lkNotifierService,
                                   CalculatedAttributesHelper calculatedAttributesHelper,
                                   ParseAttrValuesHelper parseAttrValuesHelper,
                                   NsiDadataService nsiDadataService,
                                   UserPersonalData personalData,
                                   DictionaryFilterService dictionaryFilterService,
                                   NsiDictionaryService nsiDictionaryService,
                                   NsiDictionaryFilterHelper nsiDictionaryFilterHelper) {
        this.lkNotifierService = lkNotifierService;
        this.calculatedAttributesHelper = calculatedAttributesHelper;
        this.parseAttrValuesHelper = parseAttrValuesHelper;
        this.nsiDadataService = nsiDadataService;
        this.personalData = personalData;
        this.dictionaryFilterService = dictionaryFilterService;
        this.nsiDictionaryService = nsiDictionaryService;
        this.nsiDictionaryFilterHelper = nsiDictionaryFilterHelper;
    }

    @Override
    protected void postProcess(FieldComponent component, ScenarioDto scenarioDto, String value) {
        if(component.isSendAnalytics()){
            String regionOkato = null;
            Map<String, Object> valueMap = jsonProcessingService.fromJson(value, new TypeReference<>() {});
            if (valueMap.containsKey(REG_OKATO_ATTR)) {
                regionOkato = String.valueOf(valueMap.get(REG_OKATO_ATTR));
            }
            lkNotifierService.updateOrderRegion(scenarioDto.getOrderId(), regionOkato);
        }
    }

    @Override
    public boolean shouldBeSavedInCachedAnswers(FieldComponent component, ScenarioDto scenarioDto) {
        return !Boolean.parseBoolean(String.valueOf(component.getAttrs().getOrDefault(PREV_STEP_CLEAN_CACHE_ATTR, false)));
    }

    protected Map<String, Object> getAddressData(FieldComponent component, ScenarioDto scenarioDto, MapServiceAttributes attrsDto) {
        if (nonNull(attrsDto) && nonNull(attrsDto.getAddressString())) {
            String address = getAddress(component, scenarioDto, attrsDto.getAddressString());
            if (StringUtils.hasText(address)) {
                DadataAddressResponse dadataAddressResponse = nsiDadataService.getAddress(address);
                if (dadataAddressResponse.getError().getCode() != 0) return Collections.emptyMap();

                DadataAddressResponse regionDadataAddressResponse = null;
                Set<String> fields = getPreSetFields(attrsDto);
                if (fields.isEmpty() || fields.contains(REG_OKATO_ATTR)) {
                    Optional<DadataAddressElement> regionElement = dadataAddressResponse.getAddress().getElements()
                            .stream()
                            .filter(el -> nonNull(el.getLevel()) && FIAS_REGION_LEVEL_CODE == el.getLevel())
                            .findFirst();
                    if (regionElement.isPresent()) {
                        regionDadataAddressResponse = nsiDadataService.getAddressByFiasCode(regionElement.get().getFiasCode());
                        if (regionDadataAddressResponse.getError().getCode() != 0) return Collections.emptyMap();
                    }
                }
                return getAddressPreSetComponentValues(attrsDto, dadataAddressResponse, regionDadataAddressResponse, scenarioDto, component);
            }
        }
        return Collections.emptyMap();
    }

    /**
     * Извлекает адрес в зависимости от типа атрибута
     *
     * @param component        компонент извлечения
     * @param scenarioDto      сценарий услуги
     * @param addressAttribute мапа атрибутов адреса
     * @return строка, предположительно, содержащая адрес. Не точно, так как REF или CALC могут указывать на любое значение
     */
    private String getAddress(FieldComponent component, ScenarioDto scenarioDto, AttrValue addressAttribute) {
        switch (addressAttribute.getType()) {
            case CALC:
                return calculatedAttributesHelper.getCalculatedValue(addressAttribute, component, scenarioDto);
            case REF:
                return parseAttrValuesHelper.getStringValueOfRefAttribute(addressAttribute.asStringValue(), scenarioDto);
            case ARGUMENT:
                return component.getArgument(addressAttribute.asStringValue());
            default:
                return "";
        }
    }

    /**
     * Предзаполнить значение компонента
     *
     * @return - строка, содержащая значение в формате json объекта
     */
    private Map<String, Object> getAddressPreSetComponentValues(MapServiceAttributes attributes,
                                                                DadataAddressResponse dadataAddressResponse,
                                                                DadataAddressResponse regionDadataAddressResponse,
                                                                ScenarioDto scenarioDto,
                                                                FieldComponent component) {
        Map<String, Object> presetValues = new HashMap<>();
        Set<String> fields = getPreSetFields(attributes);
        if (nonNull(dadataAddressResponse) && nonNull(dadataAddressResponse.getAddress())
                && nonNull(dadataAddressResponse.getAddress().getElements())
                && !dadataAddressResponse.getAddress().getElements().isEmpty()) {
            if (fields.contains(REG_CODE_ATTR)) {
                String kladrCode = dadataAddressResponse.getAddress().getElements().get(0).getKladrCode();
                if (nonNull(kladrCode) && !kladrCode.isBlank()) {
                    presetValues.put(REG_CODE_ATTR, "R".concat(kladrCode.substring(0, 2)));
                }
            }

            if (Objects.nonNull(attributes.getMvdFilters())) {
                addMvdSourceToPresetValues(dadataAddressResponse, presetValues, attributes.getMvdFilters(), scenarioDto, component);
            }

            presetValues.put(GEO_LAT_ATTR, dadataAddressResponse.getGeo_lat());
            presetValues.put(GEO_LON_ATTR, dadataAddressResponse.getGeo_lon());
            presetValues.put(OKATO_ATTR_NAME, dadataAddressResponse.getOkato());
            presetValues.put(OKTMO_TERRITORY_8_ATTR_NAME, getOktmo(attributes, dadataAddressResponse));
            presetValues.put(OKTMO_TERRITORY_11_ATTR_NAME, dadataAddressResponse.getOktmo());

            presetValues.put(ADDRESS, dadataAddressResponse.getAddress().getFullAddress());
            presetValues.put(ADDRESS_FIAS, dadataAddressResponse.getAddress().getFiasCode());
        }
        if (nonNull(regionDadataAddressResponse)) {
            if (fields.contains(REG_OKATO_ATTR)) {
                presetValues.put(REG_OKATO_ATTR, regionDadataAddressResponse.getOkato());
            }
            if (Objects.isNull(dadataAddressResponse)) {
                presetValues.put(GEO_LAT_ATTR, regionDadataAddressResponse.getGeo_lat());
                presetValues.put(GEO_LON_ATTR, regionDadataAddressResponse.getGeo_lon());
            }
        }
        return presetValues;
    }

    private void addMvdSourceToPresetValues(DadataAddressResponse dadataAddressResponse,
                                            Map<String, Object> presetValues, List<MvdFilter> mvdFilters,
                                            ScenarioDto scenarioDto, FieldComponent component) {
        final var fiasLevelMap = dadataAddressResponse.getAddress()
                .getElements()
                .stream()
                .filter(IS_FIRST_LEVEL_ELEMENT_PREDICATE.or(IS_FOUR_LEVEL_ELEMENT_PREDICATE))
                .collect(toMap(addressElement -> "fiasLevel" + addressElement.getLevel(), DadataAddressElement::getFiasCode));

        final var mvdSource = mvdFilters.stream()
                .filter(filter -> Objects.nonNull(filter.getFiasList()))
                .filter(filter -> processMvdSourceCondition(filter.getExtraConditionExpr(), component, scenarioDto))
                .filter(filter -> filter.getFiasList().contains(fiasLevelMap.get("fiasLevel1"))
                        || filter.getFiasList().contains(fiasLevelMap.get("fiasLevel4"))
                        || filter.getFiasList().contains("*"))
                .findFirst()
                .orElse(MvdFilter.EMPTY_FILTER)
                .getValue();
        if (StringUtils.hasText(mvdSource)) {
            presetValues.put(MVD_SOURCE_ATTR_NAME, mvdSource);
        }
    }

    /** Расчет параметра применимости фильтра. Т.к. расширение на СМЭВ 3 работает только для нового образца */
    private boolean processMvdSourceCondition(String expression, FieldComponent component, ScenarioDto scenarioDto) {
        return Objects.isNull(expression) ||
                Boolean.parseBoolean(calculatedAttributesHelper.processCalculationExpression(expression, component, scenarioDto));
    }

    /** @see <a href="https://jira.egovdev.ru/browse/EPGUCORE-53888">Обработка ОКТМО перед запросом</a> */
    private String getOktmo(MapServiceAttributes attributes, DadataAddressResponse dadataAddressResponse) {
        String originOktmo = dadataAddressResponse.getOktmo();
        if (StringUtils.hasText(originOktmo)) {
            if (checkLevelUnit(attributes.getFederalLevelUnitsFias(), dadataAddressResponse, IS_FIRST_LEVEL_ELEMENT_PREDICATE)) {
                return originOktmo.substring(0, 2) + "000000";
            }
            if (checkLevelUnit(attributes.getDistrictLevelUnitsFias(), dadataAddressResponse, IS_FOUR_LEVEL_ELEMENT_PREDICATE)) {
                return originOktmo.substring(0, 5) + "000";
            }
            return originOktmo.substring(0, 3) + "00000";
        }
        return originOktmo;
    }

    private boolean checkLevelUnit(Set<String> levelUnitsFias,
                                   DadataAddressResponse dadataAddressResponse,
                                   Predicate<DadataAddressElement> predicate) {
        if (CollectionUtils.isEmpty(levelUnitsFias)) return false;
        DadataAddress dadataAddress = dadataAddressResponse.getAddress();
        Optional<DadataAddressElement> addressElement = dadataAddress.getElements()
                .stream()
                .filter(predicate)
                .findFirst();
        return addressElement.isPresent() && levelUnitsFias.contains(addressElement.get().getFiasCode());
    }

    protected Map<String, Object> getPersonalDataValues(ScenarioDto scenarioDto, MapServiceAttributes attributes) {
        Map<String, Object> result = new HashMap<>();
        if (nonNull(attributes)) {
            Set<String> fields = getPreSetFields(attributes);
            if (fields.contains(USER_ID_FIELD_NAME)) {
                result.put(USER_ID_FIELD_NAME, personalData.getUserId());
            }
            if (fields.contains(ORDER_ID_FIELD_NAME)) {
                result.put(ORDER_ID_FIELD_NAME, scenarioDto.getOrderId());
            }

            AttrValue personalDataAttr = attributes.getPersonalData();
            if (nonNull(personalDataAttr) && nonNull(personalDataAttr.getValue())) {
                String personalData = parseAttrValuesHelper.getStringValueOfRefAttribute(personalDataAttr.asStringValue(), scenarioDto);
                if (Strings.isNotBlank(personalData)) {
                    Map<String, Object> personalDataMap = jsonProcessingService.fromJson(personalData, new TypeReference<>() {});
                    if (personalDataMap.containsKey(STORED_VALUES_ATTRIBUTE_NAME)) {
                        Map<String, Object> storedValuesMap = (Map<String, Object>) personalDataMap.get(STORED_VALUES_ATTRIBUTE_NAME);
                        if (nonNull(storedValuesMap)) {
                            if (fields.contains(FIRST_NAME_ATTR)) {
                                result.put(FIRST_NAME_ATTR, storedValuesMap.get(FIRST_NAME_ATTR));
                            }
                            if (fields.contains(LAST_NAME_ATTR)) {
                                result.put(LAST_NAME_ATTR, storedValuesMap.get(LAST_NAME_ATTR));
                            }
                            if (fields.contains(MIDDLE_NAME_ATTR)) {
                                result.put(MIDDLE_NAME_ATTR, storedValuesMap.get(MIDDLE_NAME_ATTR));
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /* TODO: Кандидат на обощение*/
    private Set<String> getPreSetFields(MapServiceAttributes attributes) {
        List<PresetField> presetFields = attributes.getFields();
        return nonNull(presetFields)
                ? presetFields.stream().map(PresetField::getFieldName).collect(toSet())
                : emptySet();
    }
}
