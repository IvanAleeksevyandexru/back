package ru.gosuslugi.pgu.pgu_common.nsi.service;

import org.springframework.http.HttpHeaders;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionary;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiDictionaryItem;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.NsiSuggestDictionary;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterRequest;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;

public interface NsiDictionaryService {

    Optional<NsiDictionaryItem> getDictionaryItemByValue(String dictionaryName, String attributeName, String attributeValue);

    NsiDictionary getDictionaryItemForMapsByFilter(String dictionaryName, NsiDictionaryFilterRequest filterRequest);

    NsiDictionary getDictionaryItemForMapsByFilter(String dictionaryName, NsiDictionaryFilterRequest filterRequest, HttpHeaders headers);

    NsiDictionary getDictionary(String dictionaryName, NsiDictionaryFilterRequest filterRequest);

    /**
     * Возвращает справочник операторов сотовой связи.
     * Их там до 10 штук
     * @return NsiDictionary
     */
    NsiDictionary getGepsDictionary();

    Future<NsiDictionary> asyncGetDictionaryItemForMapsByFilter(String dictionaryName, NsiDictionaryFilterRequest filterRequest);

    /**
     * Методы для работы с nsi-suggest справочниками
     */
    Map<String, Object> getNsiDictionaryItemByValue(String dictionaryName, String attributeName, String attributeValue);

    NsiSuggestDictionary getNsiSuggestDictionary(String dictionaryName, NsiDictionaryFilterRequest filterRequest);
}
