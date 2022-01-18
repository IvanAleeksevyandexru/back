package ru.gosuslugi.pgu.fs.component.dictionary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.atc.idecs.common.util.ws.type.Attribute;
import ru.atc.idecs.common.util.ws.type.AttributeType;
import ru.atc.idecs.common.util.ws.type.AttributeValue;
import ru.atc.idecs.refregistry.ws.Condition;
import ru.atc.idecs.refregistry.ws.ListRefItemsRequest;
import ru.atc.idecs.refregistry.ws.ListRefItemsResponse;
import ru.atc.idecs.refregistry.ws.LogicalUnionKind;
import ru.atc.idecs.refregistry.ws.LogicalUnionPredicate;
import ru.atc.idecs.refregistry.ws.Predicate;
import ru.atc.idecs.refregistry.ws.RefItem;
import ru.atc.idecs.refregistry.ws.SimplePredicate;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.common.core.exception.NsiExternalException;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.dto.ApplicantAnswer;
import ru.gosuslugi.pgu.dto.ScenarioDto;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;
import ru.gosuslugi.pgu.fs.common.service.impl.ComputeDictionaryItemService;
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil;
import ru.gosuslugi.pgu.fs.common.utils.NsiUtils;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.equipment.AttributeLink;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.equipment.EquipmentChoiceDto;
import ru.gosuslugi.pgu.fs.component.dictionary.dto.equipment.EquipmentItem;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.gosuslugi.pgu.components.ComponentAttributes.DICTIONARY_NAME_ATTR;
import static ru.gosuslugi.pgu.components.ComponentAttributes.ORIGINAL_ITEM;
import static ru.gosuslugi.pgu.components.ComponentAttributes.VALUE_ATTR;
import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.EquipmentChoice;

/**
 * Компонент "Выбор оборудования" осуществляет последовательные rest-запросы к nsi-справочникам,
 * подставляет атрибуты в последующие запросы в '${}', копирует поэлементно атрибуты предыдущего ответа в следующий.
 * В случае нескольких элементов, в следующий запрос динамически подставляется фильтр union с OR unionKind вместо simple.
 * @see <a href="https://confluence.egovdev.ru/pages/viewpage.action?pageId=193312111">Компонент "Выбор оборудования"</a>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EquipmentChoiceComponent extends AbstractComponent<EquipmentChoiceDto> {

    /** Паттерн поиска имён переменных. */
    private final static Pattern VARIABLE_REG_EXP = Pattern.compile("\\$\\{(.*?)}");

    /** Список фильтров оборудования для nsi-запросов. */
    private final static String EQUIPMENT_FILTERS = "equipmentFilters";
    /** Дополнительный nsi-запрос */
    private final static String ADDITIONAL_REQUEST = "additionalRequest";
    /** Имя атрибута, значения которого нужно дополнительно сохранить. */
    private final static String ATTR_NAME = "attrName";
    /** Тело nsi-запроса. */
    private final static String REQUESTBODY_ATTR = "requestBody";
    /** Список дополнительно сохраняемых атрибутов, которые нужно присоединить из предыдущего запроса. */
    private final static String ADDITIONAL_SAVE_ATTRS = "additionalSave";

    private final static String CATEGORIES_LIST = "categories";
    private final static String LIST_ATTR = "list";
    /** Атрибут, под который складываем конечный результат запросов. */
    private final static String RESULT_ATTR = "result";

    private final ComputeDictionaryItemService dictionaryItemService;

    @Override
    public ComponentType getType() {
        return EquipmentChoice;
    }

    @Override
    public void preProcess(FieldComponent component) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> equipmentFilters = (List<Map<String, Object>>) component.getAttrs().get(EQUIPMENT_FILTERS);
        if (CollectionUtils.isEmpty(equipmentFilters)) {
            throw new FormBaseException(String.format("Фильтры в %s отсутствуют", EQUIPMENT_FILTERS));
        }
        // внешняя мапа - номер фильтра, внутри список мап - имена и значения полученных в ответе атрибутов у каждого полученного элемента
        Map<Integer, List<EquipmentItem>> equipmentItemsMap = new HashMap<>();
        Map<String, Object> firstFilter = equipmentFilters.get(0);
        checkAttributes(firstFilter, 0);
        ListRefItemsRequest firstRequest = JsonProcessingUtil.fromJson((String) firstFilter.get(REQUESTBODY_ATTR), ListRefItemsRequest.class);
        ListRefItemsResponse firstResponse = dictionaryItemService.getDictionaryItems(firstRequest, (String) firstFilter.get(DICTIONARY_NAME_ATTR));

        List<EquipmentItem> equipmentItems = createEquipmentItems(firstResponse.getItems());
        putToEquipmentMap(equipmentItems, equipmentItemsMap, 0);

        @SuppressWarnings("unchecked")
        Map<String, Object> additionalMap = (Map<String, Object>) component.getAttrs().get(ADDITIONAL_REQUEST);
        Map<String, Object> additionalAttrsMap = Optional.ofNullable(additionalMap).map(map -> {
            String requestJson = (String) additionalMap.get(REQUESTBODY_ATTR);
            Map<String, Set<Object>> replacementValues = getReplacementMap(equipmentItemsMap.get(0), requestJson);
            ListRefItemsRequest additionalRequest = JsonProcessingUtil.fromJson(requestJson, ListRefItemsRequest.class);
            Optional<AttributeLink> attributeLink = Optional.ofNullable(additionalRequest).map(ListRefItemsRequest::getFilter).map(Predicate::getUnion).map(LogicalUnionPredicate::getSubs)
                    .stream().flatMap(Collection::stream)
                    .filter(predicate -> predicate.getUnion() != null)
                    .map(predicate -> modifyUnionPredicate(replacementValues, predicate.getUnion()))
                    .filter(attrLink -> !AttributeLink.NULL.equals(attrLink))
                    .findFirst();
            String dictionaryName = (String) additionalMap.get(DICTIONARY_NAME_ATTR);
            ListRefItemsResponse additionalResponse = dictionaryItemService.getDictionaryItems(additionalRequest, dictionaryName);
            List<EquipmentItem> additionalItems = createEquipmentItems(additionalResponse.getItems());
            String attrName = (String) additionalMap.get(ATTR_NAME);
            Set<String> ids = additionalItems.stream().map(item -> item.getAttributeValues().get(attrName).toString()).collect(Collectors.toCollection(LinkedHashSet::new));
            Map<String, Object> additionalAttrs = Collections.singletonMap("CONC_SERVICE_TYPE_IDS", ids);
            return additionalAttrs;
        }).orElse(null);


        int equipmentFilterSize = equipmentFilters.size();
        for (int i = 1; i < equipmentFilterSize; i++) {
            Map<String, Object> filter = equipmentFilters.get(i);
            checkAttributes(filter, i);
            String requestJson = (String) filter.get(REQUESTBODY_ATTR);
            String dictionaryName = (String) filter.get(DICTIONARY_NAME_ATTR);

            List<EquipmentItem> previousAttributeItemList = equipmentItemsMap.get(i - 1);
            Map<String, Set<Object>> replacementValues = getReplacementMap(previousAttributeItemList, requestJson);
            ListRefItemsRequest request = JsonProcessingUtil.fromJson(requestJson, ListRefItemsRequest.class);
            Optional<AttributeLink> attributeLink = Optional.ofNullable(request).map(ListRefItemsRequest::getFilter).map(Predicate::getUnion).map(LogicalUnionPredicate::getSubs)
                    .stream().flatMap(Collection::stream)
                    .filter(predicate -> predicate.getUnion() != null)
                    .map(predicate -> modifyUnionPredicate(replacementValues, predicate.getUnion()))
                    .filter(attrLink -> !AttributeLink.NULL.equals(attrLink))
                    .findFirst();
            ListRefItemsResponse response = dictionaryItemService.getDictionaryItems(request, dictionaryName);
            List<EquipmentItem> equipmentItemList = createEquipmentItems(response.getItems());
            putToEquipmentMap(equipmentItemList, equipmentItemsMap, i);

            enrichCurrentAttributesByPrevious(filter, i, equipmentItemsMap, attributeLink);
        }
        List<EquipmentItem> itemList = equipmentItemsMap.get(equipmentFilterSize - 1);
        EquipmentChoiceDto dto = new EquipmentChoiceDto(itemList, additionalAttrsMap);
        component.getAttrs().put(RESULT_ATTR, dto);
    }

    /**
     * Строит список атрибутов со значениями по каждому элементу из ответа от nsi-справочника
     * @param responseItems список элементов ответа nsi-справочника
     */
    private List<EquipmentItem> createEquipmentItems(List<RefItem> responseItems) {
        return Optional.of(responseItems).stream().flatMap(Collection::stream)
                .map(refItem -> new EquipmentItem(refItem.getValue(), refItem.getTitle(), getAttributeValues(refItem)))
                .collect(Collectors.toList());
    }

    /**
     * Добавляет список ответов от nsi-справочника ы мапу всех атрибутов для данной итерации.
     * @param itemList список атрибутов со значениями по каждому элементу из ответа от nsi-справочника
     * @param equipmentItemsMap мапа всех атрибутов со значениями
     * @param iteration номер фильтра-итерации
     */
    private void putToEquipmentMap(List<EquipmentItem> itemList, Map<Integer, List<EquipmentItem>> equipmentItemsMap, int iteration) {
        equipmentItemsMap.put(iteration, itemList);
    }

    /**
     * Добавляет в текущие атрибуты и ответ от nsi справочника набор атрибутов с прошлой итерации, указанных в {@link #ADDITIONAL_SAVE_ATTRS} атрибуте
     * @param equipmentFilter фильтр для оборудования
     * @param filterNumber номер фильтра в списке фильтров
     * @param equipmentItemsMap мапа всех атрибутов со значениями
     * @param linkOptional опциональный линк, связывающий имя атрибута из значений предыдущего ответа от nsi-справочника
     *                                 и имя атрибута в фильтре из текущего запроса, в которое нужно подставить значения из предыдущего ответа
     */
    private void enrichCurrentAttributesByPrevious(Map<String, Object> equipmentFilter, int filterNumber, Map<Integer, List<EquipmentItem>> equipmentItemsMap,
                                                   Optional<AttributeLink> linkOptional) {
        List<EquipmentItem> previousAttributeItemList = equipmentItemsMap.get(filterNumber - 1);
        @SuppressWarnings("unchecked")
        List<String> additionalSaveList = (List<String>) equipmentFilter.getOrDefault(ADDITIONAL_SAVE_ATTRS, null);
        if (!CollectionUtils.isEmpty(additionalSaveList) && linkOptional.isPresent()) {
            List<EquipmentItem> currentAttributeItemList = equipmentItemsMap.get(filterNumber);// список атрибутов и их значений из items текущего респонса
            AttributeLink attributeLink = linkOptional.get();
            String nextAttrName = attributeLink.getNextAttrName();
            String previousAttrName = attributeLink.getPreviousAttrName();
            for (EquipmentItem currentItem : currentAttributeItemList) {
                Map<String, Object> itemAttrs = currentItem.getAttributeValues();
                String currentAttrValue = itemAttrs.get(nextAttrName).toString();
                for (EquipmentItem previousItem : previousAttributeItemList) {
                    Map<String, Object> previousItemAttrs = new HashMap<>(previousItem.getAttributeValues());
                    String previousItemValue = previousItemAttrs.get(previousAttrName).toString();
                    if (currentAttrValue.equals(previousItemValue)) {
                        previousItemAttrs.keySet().retainAll(additionalSaveList); // из копии мапы атрибутов предыдущей итерации удалятся все, кроме тех, которые в списке ADDITIONAL_SAVE_ATTRS
                        itemAttrs.putAll(previousItemAttrs);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Осуществляет копирование нужного количества simple-фильтров по шаблону, указанному в {@link #REQUESTBODY_ATTR}.
     * @param replacementValues мапа имени атрибута на список его значений из каждого элемента ответа nsi-сервиса для подстановки в шаблон nsi-запроса набора фильтров
     * @param logicalUnionPredicate union-фильтр из тела запроса, в который динамически добавляется нужное количество простых фильтров со значениями
     * @return линк, связывающий имя атрибута из значений предыдущего ответа от nsi-справочника
     * и имя атрибута в фильтре из текущего запроса, в которое нужно подставить значения из предыдущего ответа
     */
    private AttributeLink modifyUnionPredicate(Map<String, Set<Object>> replacementValues, LogicalUnionPredicate logicalUnionPredicate) {
        AttributeLink attributeLink = AttributeLink.NULL;
        LogicalUnionKind internalUnionKind = logicalUnionPredicate.getUnionKind();
        Predicate dynamicPredicate = logicalUnionPredicate.getSubs().get(0);
        if (dynamicPredicate != null && dynamicPredicate.getSimple() != null) {
            logicalUnionPredicate.getSubs().remove(dynamicPredicate);

            SimplePredicate simplePredicate = dynamicPredicate.getSimple();

            String attributeName = simplePredicate.getAttributeName();
            String attrValueForReplacement = NsiUtils.getValue(simplePredicate);
            AttributeValue value = simplePredicate.getValue();
            AttributeType type = value.getTypeOfValue();

            Condition condition = simplePredicate.getCondition();

            Set<Object> values = replacementValues.get(attrValueForReplacement);

            Matcher matcher = VARIABLE_REG_EXP.matcher(attrValueForReplacement);
            if (matcher.find()) {
                attrValueForReplacement = matcher.group(1);
            }
            LogicalUnionPredicate unionPredicate = NsiUtils.getUnionPredicate(internalUnionKind, attributeName, type, condition, values.toArray(new Object[0]));
            logicalUnionPredicate.getSubs().add(new Predicate(unionPredicate));
            attributeLink = new AttributeLink(attributeName, attrValueForReplacement);
        }
        return attributeLink;
    }

    /**
     * Строит мапу списков со значениями атрибутов из шаблона тела nsi-запроса.
     * Ведётся поиск переменных, обёрнутых в '${}' и из каждого элемента ответа достаётся значение атрибута и добавляется в список.
     * @param previousAttributeItemList Список всех значений атрибутов из ответа с предыдущего шага, которые вернул nsi-сервис
     * @param requestJson строка с шаблоном тела nsi-запроса, в шаблоне '${}' указывается имя атрибута с предыдущего шага
     * @return мапа имени атрибута на список его значений из каждого элемента ответа nsi-сервиса для подстановки в шаблон nsi-запроса набора фильтров.
     */
    private Map<String, Set<Object>> getReplacementMap(List<EquipmentItem> previousAttributeItemList, String requestJson) {
        Matcher expressionMatcher = VARIABLE_REG_EXP.matcher(requestJson);
        Map<String, Set<Object>> replacementValues = new LinkedHashMap<>();
        while (expressionMatcher.find()) {
            String attributeNameForReplacement = expressionMatcher.group(1);

            Set<Object> values = previousAttributeItemList.stream().map(attrItem -> attrItem.getAttributeValues().get(attributeNameForReplacement))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            replacementValues.put("${" + attributeNameForReplacement + '}', values);
        }
        return replacementValues;
    }

    /**
     * Получение мапы атрибутов и их значений для одного элемента ответа.
     * Стандартный метод {@link RefItem#getAttributeValues()} не находит половину атрибутов и работает плохо.
     * Данный метод заменяет его.
     * @param refItem элемент ответа от nsi-сервиса.
     * @return мапа атрибутов со значениями с сохранением порядка их получения из сервиса.
     */
    private Map<String, Object> getAttributeValues(RefItem refItem) {
        List<Attribute> attributeList = refItem.getAttributes();
        Map<String, Object> result =  attributeList.stream()
                .collect(() -> new LinkedHashMap<>(attributeList.size() + attributeList.size() / 3 + 1),
                        (map, attr) -> map.put(attr.getName(), Optional.of(attr).map(Attribute::getValue).map(AttributeValue::getValue).orElse("")),
                        LinkedHashMap::putAll);
        return result;
    }

    /**
     * Проверяет наличие ненулевых атрибутов в фильтре по оборудованию.
     * @param equipmentFilter единичный фильтр оборудования, представляет собой данные, необходимые для осуществления запроса к nsi-справочнику
     *                       и рекуррентной подстановки параметров в тело запроса из предыдущего ответа
     * @param iteration номер фильтра в списке
     */
    private void checkAttributes(Map<String, Object> equipmentFilter, int iteration) {
        Optional<String> nullFirsAttr = Stream.of(DICTIONARY_NAME_ATTR, REQUESTBODY_ATTR).filter(attr -> Objects.isNull(equipmentFilter.get(attr))).findFirst();
        if (nullFirsAttr.isPresent()) {
            throw new FormBaseException(String.format("Отсутствует атрибут %s на итерации %d", nullFirsAttr.get(), iteration));
        }
    }

    @Override
    protected void validateAfterSubmit(Map<String, String> incorrectAnswers, Entry<String, ApplicantAnswer> entry, FieldComponent fieldComponent) {
        String entryValue = AnswerUtil.getValue(entry);
        if(StringUtils.isBlank(entryValue)) {
            incorrectAnswers.put(entry.getKey(), "Выбор оборудования не произведён");
            return;
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> equipmentFilters = (List<Map<String, Object>>) fieldComponent.getAttrs().get(EQUIPMENT_FILTERS);
        List<String> dictionaryNames = equipmentFilters.stream().map(map -> (String) map.get(DICTIONARY_NAME_ATTR)).collect(Collectors.toList());
        String dictionaryName = dictionaryNames.get(dictionaryNames.size() - 1);
        Map<Object, Object> entryMap = AnswerUtil.toMap(entry, true);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> categoriesList = (List<Map<String, Object>>)entryMap.get(CATEGORIES_LIST);
        categoriesList.forEach(category -> {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>)category.get(LIST_ATTR);
            List<String> values = list.stream().map(it -> getDictionaryValue(it, VALUE_ATTR)).collect(Collectors.toList());
            values.forEach( value -> {
                try {
                    Optional<RefItem> refItem = dictionaryItemService.getDictionaryItem(VALUE_ATTR, value, Condition.EQUALS, dictionaryName);
                    if (refItem.isEmpty()) {
                        incorrectAnswers.put(entry.getKey(), String.format("Не найдено значение %s в справочнике НСИ %s", value, dictionaryName));
                        return;
                    }
                } catch (NsiExternalException e) {
                    String message = e.getValueMessage();
                    incorrectAnswers.put(entry.getKey(), "Справочник " + dictionaryName + ", значение " + value + ". Сообщение: " + message);
                    return;
                }
            });
        });
    }

    /**
     * Получение значения атрибута из элемента ответа, который посылается в {@link ScenarioDto#getCurrentValue()} c UI.
     * @param item одно выбранное пользователем значение
     * @param attr имя атрибута, значение которого требуется
     * @return значение атрибута
     */
    private String getDictionaryValue(Object item, String attr) {
        Map<String, Object> map = (Map<String, Object>) item;
        Map<String, Object> originalItemMap = (Map<String, Object>) map.getOrDefault(ORIGINAL_ITEM, Map.of());
        return originalItemMap.getOrDefault(attr, "").toString();
    }
}
