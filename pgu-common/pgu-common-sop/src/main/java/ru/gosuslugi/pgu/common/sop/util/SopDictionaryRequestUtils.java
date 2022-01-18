package ru.gosuslugi.pgu.common.sop.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.common.sop.dto.Mode;
import ru.gosuslugi.pgu.common.sop.dto.SopDictionaryRequest;
import ru.gosuslugi.pgu.common.sop.dto.SopFiltersItem;
import ru.gosuslugi.pgu.common.sop.dto.SopPageLimit;
import ru.gosuslugi.pgu.common.sop.dto.SopRequestFilter;
import ru.gosuslugi.pgu.common.sop.dto.SopRequestProjection;
import ru.gosuslugi.pgu.common.sop.dto.SopRequestQuery;
import ru.gosuslugi.pgu.common.sop.service.SopUid;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *  Утилитарный класс для вспомогательных методов рабоботающих с {@link SopDictionaryRequest} и другими классами пакета.
 */
@UtilityClass
public class SopDictionaryRequestUtils {

    /**
     * Запрос с фильтром по полному совпадению со значением атрибута.
     * @param sopUid справочник
     * @param attributeName имя атрибута в фильтре
     * @param attributeValue значение атрибута в фильтре
     * @return ответ от сервиса СОП с найденными значениями
     */
    public SopDictionaryRequest requestWithFilter(SopUid sopUid, String attributeName, String attributeValue) {
        String sopSourceUid = sopUid.getSopSourceUid();
        String filterColumnUid = sopUid.getColumnUid(attributeName);
        SopDictionaryRequest requestBody = SopDictionaryRequest.builder()
                .sourceUid(sopSourceUid)
                .limit(SopPageLimit.with().pageSize(2).pageNumber(1).build())
                .projection(SopRequestProjection.with().mode(Mode.INCLUDE).columnUids(new ArrayList<>(sopUid.getColumnUids())).build())
                .filter(
                        SopRequestFilter.with().filters(
                                (StringUtils.isEmpty(filterColumnUid) || StringUtils.isEmpty(attributeValue)) ? null :
                                List.of(SopFiltersItem.with().columnUid(filterColumnUid).value(attributeValue).build())
                        ).build()
                ).build();

        return requestBody;
    }

    /**
     * Запрос с фильтром по частичному совпадению со значением атрибута, учитываются опечатки, а также инверсия раскладки
     * @param sopUid справочник
     * @param projectionColumnNames имена колонок среди которых осуществляется запрос
     * @param queryColumnNames  имена колонок среди которых осуществляется поиск по запросу
     * @param attributeValue значение атрибута в фильтре
     * @param levenstein число допустимых ошибок(опечаток) в каждом слове
     * @param inversion инверсия набора значения (например на английском вместо русского)
     * @param searchAnyWords Признак поиска всех слов из запроса
     * @return ответ от сервиса СОП с найденными значениями
     */
    public SopDictionaryRequest requestWithQuery(SopUid sopUid, Set<String> projectionColumnNames, Set<String> queryColumnNames, String attributeValue,
                                                 int pageSize, int levenstein, boolean inversion, boolean searchAnyWords) {
        String sopSourceUid = sopUid.getSopSourceUid();
        List<String> projectionColumnUids = new ArrayList<>(sopUid.getColumnUids(projectionColumnNames));
        List<String> queryColumnUids = new ArrayList<>(sopUid.getColumnUids(queryColumnNames));
        SopRequestFilter filter = SopRequestFilter.with().query(
                (CollectionUtils.isEmpty(queryColumnUids) || StringUtils.isEmpty(attributeValue)) ? null :
                        SopRequestQuery.with().query(attributeValue).columnUids(queryColumnUids).searchAnyWord(searchAnyWords).build()
        ).build();
        SopDictionaryRequest requestBody = SopDictionaryRequest.builder()
                .sourceUid(sopSourceUid)
                .inversion(inversion)
                .levenshtein(levenstein)
                .limit(SopPageLimit.with().pageSize(pageSize).pageNumber(1).build())
                .projection(SopRequestProjection.with().mode(Mode.INCLUDE).columnUids(projectionColumnUids).build())
                .filter(filter)
                .build();

        return requestBody;
    }
}
