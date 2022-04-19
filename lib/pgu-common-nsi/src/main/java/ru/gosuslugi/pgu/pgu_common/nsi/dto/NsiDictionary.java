package ru.gosuslugi.pgu.pgu_common.nsi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import ru.gosuslugi.pgu.common.core.exception.dto.ExternalError;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * DTO for storing dictionaries from NSI
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class NsiDictionary {


    /** Мапа, по имени поля возвращающая ссылку на соответствующий get-метод. */
    @JsonIgnore
    private static final Map<String, Function<NsiDictionaryItem, String>> FIELD_TO_STRING_MAPPER = Map.of(
            "value", NsiDictionaryItem::getValue,
            "title", NsiDictionaryItem::getTitle
    );

    private ExternalError error;
    String total;
    List<NsiDictionaryItem> items;

    public Optional<NsiDictionaryItem> getItem(String value) {
        Optional<NsiDictionaryItem> first = items.stream().filter(i -> i.getValue().equals(value)).findFirst();
        return first;
    }

    @JsonIgnore
    public Optional<NsiDictionaryItem> getItem(String attributeName, String attributeValue) {
        Function<NsiDictionaryItem, String> fieldFunction = FIELD_TO_STRING_MAPPER.getOrDefault(attributeName, item -> item.getAttributeValue(attributeName));
        if (fieldFunction == null) {
            return Optional.empty();
        }
        return items.stream().filter(item -> fieldFunction.apply(item).equals(attributeValue)).findFirst();
    }
}
