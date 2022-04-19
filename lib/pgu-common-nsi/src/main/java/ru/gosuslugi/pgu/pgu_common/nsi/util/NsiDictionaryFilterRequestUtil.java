package ru.gosuslugi.pgu.pgu_common.nsi.util;

import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilter;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiDictionaryFilterRequest;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiSimpleDictionaryFilterContainer;
import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.NsiUnionDictionaryFilterContainer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

/**
 * Утилитарный класс для парсинга @see NsiDictionaryFilterRequest и нахождения OKATO
 */
public class NsiDictionaryFilterRequestUtil {

    public static final String OKATO_ATTRIBUTE_NAME = "okato_in";

    /**
     * Private Constructor
     */
    private NsiDictionaryFilterRequestUtil() {
    }

    /**
     * Возвращает ОКАТО
     * @param filterRequest фильтр
     * @return ОКАТО или null
     */
    public static String getOkato(NsiDictionaryFilterRequest filterRequest) {

        // Лист параметров
        List<NsiSimpleDictionaryFilterContainer> parameters = Optional.ofNullable(filterRequest.getFilter())
                .map(NsiDictionaryFilterRequestUtil::toContainerList)
                .orElse(Collections.emptyList());

        // Ищем первый непустой параметр с именем атрибута ОКАТО
        return parameters
                .stream()
                .map(NsiSimpleDictionaryFilterContainer::getSimple)
                .filter(simple -> OKATO_ATTRIBUTE_NAME.equalsIgnoreCase(simple.getAttributeName()))
                .filter(simple -> nonNull(simple.getValue()) && nonNull(simple.getValue().getAttributeValue("asString")))
                .map(simple -> simple.getValue().getAttributeValue("asString").toString())
                .findFirst()
                .orElse(null);
    }

    private static List<NsiSimpleDictionaryFilterContainer> toContainerList(NsiDictionaryFilter filter) {
        if (filter instanceof NsiSimpleDictionaryFilterContainer) {
            return Collections.singletonList((NsiSimpleDictionaryFilterContainer) filter);
        }
        if (filter instanceof NsiUnionDictionaryFilterContainer) {
            return ((NsiUnionDictionaryFilterContainer) filter).getUnion().getSubs().stream()
                    .filter(subFilter -> subFilter instanceof NsiSimpleDictionaryFilterContainer)
                    .map(f -> (NsiSimpleDictionaryFilterContainer) f)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
