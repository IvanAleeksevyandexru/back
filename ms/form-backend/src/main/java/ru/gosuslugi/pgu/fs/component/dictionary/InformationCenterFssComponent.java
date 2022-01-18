package ru.gosuslugi.pgu.fs.component.dictionary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.components.ComponentAttributes;
import ru.gosuslugi.pgu.components.descriptor.types.FullAddress;
import ru.gosuslugi.pgu.components.descriptor.types.RegistrationAddress;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.component.validation.RequiredNotBlankValidation;
import ru.gosuslugi.pgu.fs.common.component.validation.ValidationRule;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.FssDataItem;
import ru.gosuslugi.pgu.fs.utils.ContextJsonUtil;
import ru.gosuslugi.pgu.fs.utils.ParseAttrValuesHelper;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionary;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionaryItem;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterRequest;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterSimple;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiFilterCondition;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiSimpleDictionaryFilterContainer;
import ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService;

import java.util.*;
import java.util.function.Function;

import static ru.gosuslugi.pgu.components.ComponentAttributes.DICTIONARY_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.VALUE_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.VALUE_NOT_FOUND_MESSAGE;
import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.InformationCenterFss;

@Slf4j
@Component
@RequiredArgsConstructor
public class InformationCenterFssComponent extends AbstractComponent<FssDataItem> {
    private static final String ADDRESS_ATTRIBUTE_NAME = "addressString";
    private static final String TO_FSS_DICTIONARY_NAME = "FSS_TO";
    private static final String REGION_CODE = "CODE";
    private static final String FSS_NAME = "NAME";
    private static final String FSS_ADDRESS = "ADDRESS";
    private static final String SIMPLE_FIELD = "simple";

    private final NsiDictionaryService nsiDictionaryService;
    private final ParseAttrValuesHelper parseAttrValuesHelper;

    @Override
    public ComponentType getType() {
        return InformationCenterFss;
    }

    @Override
    protected void preProcess(FieldComponent component, ScenarioDto scenarioDto) {
        if (Objects.nonNull(component.getAttrs()) && component.getAttrs().containsKey(ADDRESS_ATTRIBUTE_NAME)) {
            String dictionaryName = getDictionaryName(component);
            String regionCode = getAttributeFromAddress(component, scenarioDto, FullAddress::getRegionCode);

            NsiDictionaryFilterRequest requestBody = getFilterRequest(regionCode);
            NsiDictionary dictionary = nsiDictionaryService.getDictionary(dictionaryName, requestBody);

            Optional<List<NsiDictionaryItem>> optionalItems = Optional.ofNullable(dictionary).map(NsiDictionary::getItems);
            if (optionalItems.isEmpty() || optionalItems.get().isEmpty()) {
                throw new FormBaseException(String.format("Не найдено подразделение по коду региона %s", regionCode));
            }
            List<NsiDictionaryItem> nsiDictionaryItems = optionalItems.get();
            if (nsiDictionaryItems.size() > 1) {
                throw new FormBaseException(String.format("Должно возвращаться всегда единственное подразделение по коду региона %s", regionCode));
            }
            NsiDictionaryItem nsiDictionaryItem = nsiDictionaryItems.get(0);
            putPfrInComponentAttrs(nsiDictionaryItem, component);
        }

        Map<String, Object> simpleDescription = (Map<String, Object>) component.getAttrs().get(SIMPLE_FIELD);
        String html = (String) simpleDescription.get("html");
        String s = componentReferenceService.getValueByContext(html, Function.identity(),
                componentReferenceService.buildPlaceholderContext(component, scenarioDto),
                componentReferenceService.getContexts(scenarioDto));
        simpleDescription.put("html", s);

    }

    @Override
    public List<ValidationRule> getValidations() {
        return List.of(
                new RequiredNotBlankValidation("Поле обязательно для заполнения")
        );
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Map.Entry<String, ApplicantAnswer> entry, ScenarioDto scenarioDto, FieldComponent fieldComponent) {
        if (StringUtils.hasText(AnswerUtil.getValue(entry))) {
            Map.Entry<String, String> attributeEntry = ContextJsonUtil.getAttributeInDepth(jsonProcessingService, entry, null, "title");
            String value = attributeEntry.getValue();
            int i = value.indexOf("<br>");
            if (i > -1) {
                value = value.substring(0, i);
            }
            String dictionaryName = getDictionaryName(fieldComponent);
            String regionCode = getAttributeFromAddress(fieldComponent, scenarioDto, FullAddress::getRegionCode);
            Optional<NsiDictionaryItem> dictionaryItem = nsiDictionaryService.getDictionaryItemByValue(dictionaryName, VALUE_ATTR, regionCode);
            if (dictionaryItem.isEmpty()) {
                incorrectAnswers.put(entry.getKey(), String.format(VALUE_NOT_FOUND_MESSAGE, dictionaryName));
                return;
            }
            if (!value.equals(dictionaryItem.get().getAttributeValue(FSS_NAME))) {
                incorrectAnswers.put(entry.getKey(), String.format("Выбранное значение не совпадает со значением в справочнике %s", dictionaryName));
                return;
            }
        }
    }

    private static NsiDictionaryFilterRequest getFilterRequest(String regionCode) {
        if (!StringUtils.hasText(regionCode) || regionCode.length() != 2) {
            // можно было бы кинуть эксепшн, но лучше дать пользователю в таком случае выбрать отделение вручную на фронте
            log.warn("Trying to construct filter for not existed property for dictionary " + TO_FSS_DICTIONARY_NAME);
            return null;
        }

        NsiDictionaryFilterSimple simple = new NsiDictionaryFilterSimple.Builder()
                .setAttributeName(REGION_CODE)
                .setCondition(NsiFilterCondition.EQUALS.toString())
                .setStringValue(regionCode)
                .build();

        NsiSimpleDictionaryFilterContainer simpleContainer = new NsiSimpleDictionaryFilterContainer();
        simpleContainer.setSimple(simple);

        return new NsiDictionaryFilterRequest.Builder()
                .setTreeFiltering("ONELEVEL")
                .setPageNum("1")
                .setPageSize("2")
                .setSelectAttributes(List.of("*"))
                .setFilter(simpleContainer)
                .build();
    }

    private static void putPfrInComponentAttrs(NsiDictionaryItem nsiDictionaryItem, FieldComponent component) {
        if (Objects.nonNull(nsiDictionaryItem)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> simpleEntry = (LinkedHashMap<String, Object>) component.getAttrs().get("simple");
            if (Objects.isNull(simpleEntry)) {
                simpleEntry = new LinkedHashMap<>();
                component.getAttrs().put("simple", simpleEntry);
            }
            FssDataItem item = new FssDataItem();
            String name = nsiDictionaryItem.getAttributeValue(FSS_NAME);
            String address = nsiDictionaryItem.getAttributeValue(FSS_ADDRESS);
            item.setTitle(name + "<br>" + address);
            item.setValue("");
            item.setAttributeValues(nsiDictionaryItem.getAttributeValues());
            simpleEntry.put("items", Collections.singletonList(item));
        }
    }

    /**
     * Получает имя справочника из атрибута {@link ComponentAttributes#DICTIONARY_NAME_ATTR} компонента
     *
     * @param component компонент
     * @return имя справочика из атрибута или дефолтное значение {@link #TO_FSS_DICTIONARY_NAME}.
     */
    private static String getDictionaryName(FieldComponent component) {
        if (component.getAttrs() != null) {
            return component.getAttrs().getOrDefault(DICTIONARY_NAME_ATTR, TO_FSS_DICTIONARY_NAME).toString();
        }
        return TO_FSS_DICTIONARY_NAME;
    }

    /**
     * Возвращение значения атрибута, ссылка на метод передаётся в аргументе из адреса.
     *
     * @param component         компонент
     * @param scenarioDto       информация о текущем заявлении
     * @param attributeFunction функция, задающая
     * @return значение атрибута
     */
    private String getAttributeFromAddress(FieldComponent component, ScenarioDto scenarioDto, Function<FullAddress, String> attributeFunction) {
        var addressAttr = component.getAttrs().get(ADDRESS_ATTRIBUTE_NAME);
        if (Objects.nonNull(addressAttr)) {
            @SuppressWarnings("unchecked")
            Map<String, String> addressRefMap = (Map<String, String>) addressAttr;
            String address = parseAttrValuesHelper.getAttributeValue(addressRefMap, scenarioDto);
            String value = null;
            RegistrationAddress registrationAddress = JsonProcessingUtil.fromJson(address, RegistrationAddress.class);
            if (Objects.nonNull(registrationAddress) && Objects.nonNull(registrationAddress.getRegAddr())) {
                value = attributeFunction.apply(registrationAddress.getRegAddr());
            }
            if (StringUtils.isEmpty(value)) { // данный адрес может прийти из AddressInput компонента, поэтому обработаем и такой адрес
                FullAddress fullAddress = JsonProcessingUtil.fromJson(address, FullAddress.class);
                if (Objects.nonNull(fullAddress)) {
                    value = attributeFunction.apply(fullAddress);
                }
            }
            if (StringUtils.isEmpty(value)) {
                log.info(String.format("Адрес, на который ссылается компонент %s не распарсился.", component.getId()));
            }
            return value;
        }
        return null;
    }
}
