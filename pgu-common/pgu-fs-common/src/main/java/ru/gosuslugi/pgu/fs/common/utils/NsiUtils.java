package ru.gosuslugi.pgu.fs.common.utils;

import lombok.experimental.UtilityClass;
import ru.atc.idecs.common.util.ws.type.AttributeType;
import ru.atc.idecs.common.util.ws.type.AttributeValue;
import ru.atc.idecs.refregistry.ws.Condition;
import ru.atc.idecs.refregistry.ws.ListRefItemsRequest;
import ru.atc.idecs.refregistry.ws.LogicalUnionKind;
import ru.atc.idecs.refregistry.ws.LogicalUnionPredicate;
import ru.atc.idecs.refregistry.ws.Predicate;
import ru.atc.idecs.refregistry.ws.SimplePredicate;
import ru.atc.idecs.refregistry.ws.TreeFiltering;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;


@UtilityClass
/**
 * Класс-утилита для создания nsi-запросов на основе классов из refregistry-api зависимости.
 * Использование данного utility-класса предпочтительнее тем, что в нём намного полнее описание dto-объекта.
 * <ul>На эти вызовы будут заменены все вызовы из других аналогичных классов:
 *    <li>{@code ru.gosuslugi.pgu.pgu_common.gibdd.util.NsiDictionaryUtil}</li>
 *    <li>{@code ru.gosuslugi.pgu.pgu_common.nsi.service.NsiDictionaryService}</li>
 *    <li>{@code ru.gosuslugi.pgu.pgu_common.nsi.dto.*}</li>
 *    <li>{@code ru.gosuslugi.pgu.pgu_common.nsi.util.NsiDictionaryFilterRequestUtil}</li>
 *    <li>{@code ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiSimpleDictionaryFilterContainer}</li>
 * </ul>
 *
 */
public class NsiUtils {

    /**
     * Получение nsi-запроса по входящим параметрам
     * @param commonParameters общие параметры запросы, могут быть пропущены, т.к. содержат значения по умолчанию.
     * @param attributes параметры для фильтрации
     * @return nsi-запрос
     */
    public ListRefItemsRequest getSimpleRequestByAttr(Map<String, Object> commonParameters, Map<String, Entry<Object, Condition>> attributes) {
        ListRefItemsRequest requestBody = new ListRefItemsRequest();
        requestBody.setTreeFiltering((TreeFiltering) commonParameters.getOrDefault("treeFiltering", TreeFiltering.ONELEVEL));
        requestBody.setPageNum((int) commonParameters.getOrDefault("pageNum", 1));
        requestBody.setPageSize((int) commonParameters.getOrDefault("pageSize", 100));
        requestBody.setParentRefItemValue((String) commonParameters.getOrDefault("parentRefItemValue",""));
        requestBody.getSelectAttributes().add((String) commonParameters.getOrDefault("selectAttributes", "*"));
        requestBody.setTx((String) commonParameters.getOrDefault("tx", ""));
        Predicate predicate = new Predicate();
        if (attributes.size() == 1) {
            Entry<String, Entry<Object, Condition>> entry = attributes.entrySet().iterator().next();
            String attrName = entry.getKey();
            Entry<Object, Condition> internalEntry = entry.getValue();
            SimplePredicate simplePredicate = getSimplePredicate(attrName, AttributeType.STRING, internalEntry.getKey(), internalEntry.getValue());
            predicate.setSimple(simplePredicate);
        } else {
            LogicalUnionPredicate unionPredicate = new LogicalUnionPredicate();
            List<Predicate> subs = unionPredicate.getSubs();
            attributes.forEach((attrName, internalEntry) -> {
                unionPredicate.setUnionKind((LogicalUnionKind) commonParameters.getOrDefault("unionKind", LogicalUnionKind.AND));
                Predicate internalPredicate = new Predicate();
                SimplePredicate simplePredicate = getSimplePredicate(attrName, AttributeType.STRING, internalEntry.getKey(), internalEntry.getValue());
                internalPredicate.setSimple(simplePredicate);
                subs.add(internalPredicate);
            });
            predicate.setUnion(unionPredicate);
        }
        requestBody.setFilter(predicate);
        return requestBody;
    }

    public LogicalUnionPredicate getUnionPredicate(LogicalUnionKind unionKind, String attrName, AttributeType type, Condition condition, Object... attrValues) {
        LogicalUnionPredicate unionPredicate = new LogicalUnionPredicate();
        unionPredicate.setUnionKind(unionKind);
        List<Predicate> subs = unionPredicate.getSubs();
        Arrays.stream(attrValues).map(attrValue -> new Predicate(getSimplePredicate(attrName, type, attrValue, condition))).forEach(subs::add);
        return unionPredicate;
    }

    public SimplePredicate getSimplePredicate(String attrName, AttributeType type, Object attrValue, Condition condition) {
        SimplePredicate simplePredicate = new SimplePredicate();
        simplePredicate.setAttributeName(attrName);
        AttributeValue attributeValue = createValue(type, attrValue);
        simplePredicate.setValue(attributeValue);
        simplePredicate.setCondition(condition);
        simplePredicate.setCheckAllValues(false);
        return simplePredicate;
    }

    public String getValue(SimplePredicate simplePredicate) {
        return Optional.of(simplePredicate)
                .map(SimplePredicate::getValue)
                .map(AttributeValue::getValue)
                .map(Object::toString)
                .orElse("");
    }

    private AttributeValue createValue(AttributeType type, Object attrValue) {
        AttributeValue attributeValue = new AttributeValue();
        switch (type) {
            case STRING:
                attributeValue.setAsString(attrValue.toString());
                return attributeValue;
            case LONG:
                attributeValue.setAsLong(Long.parseLong(attrValue.toString()));
                return attributeValue;
            case BOOLEAN:
                attributeValue.setAsBoolean(Boolean.parseBoolean(attrValue.toString()));
                return attributeValue;
            case DECIMAL:
                attributeValue.setAsDecimal(new BigDecimal(attrValue.toString()));
                return attributeValue;
            case DATE:
            case DATETIME:
            default:
                attributeValue.setValue(attrValue);
                return attributeValue;
        }
    }
}
