package ru.gosuslugi.pgu.pgu_common.gibdd.util;

import ru.gosuslugi.pgu.pgu_common.nsi.dto.filter.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NsiDictionaryUtil {

    public static NsiDictionaryFilterRequest getFilterRequest(Map<String, String> params, String tx) {
        List<NsiDictionaryFilter> subs = new ArrayList<>();
        params.forEach((key, value) -> {
            NsiDictionaryFilterSimple simple = new NsiDictionaryFilterSimple();
            NsiDictionaryFilterSimpleValue simpleValue = new NsiDictionaryFilterSimpleValue();
            simpleValue.putAttributeValue("asString", value);
            simple.setAttributeName(key);
            simple.setCondition("EQUALS");
            simple.setValue(simpleValue);

            NsiSimpleDictionaryFilterContainer simpleContainer = new NsiSimpleDictionaryFilterContainer();
            simpleContainer.setSimple(simple);
            subs.add(simpleContainer);
        });

        NsiDictionaryFilterUnion union = new NsiDictionaryFilterUnion();
        union.setUnionKind(NsiDictionaryUnionType.AND);
        union.setSubs(subs);

        NsiUnionDictionaryFilterContainer dictionaryFilter = new NsiUnionDictionaryFilterContainer();
        dictionaryFilter.setUnion(union);

        NsiDictionaryFilterRequest.Builder requestBuilder = new NsiDictionaryFilterRequest.Builder();
        requestBuilder
                .setTreeFiltering("ONELEVEL")
                .setPageNum("1")
                .setPageSize("258")
                .setSelectAttributes(List.of("*"))
                .setFilter(dictionaryFilter);
        NsiDictionaryFilterRequest nsiRequest = requestBuilder.build();
        nsiRequest.setTx(tx);
        nsiRequest.setParentRefItemValue("");
        return nsiRequest;
    }

    public static NsiDictionaryFilterRequest getSimpleRequest() {
        NsiDictionaryFilterRequest filterRequest = new NsiDictionaryFilterRequest();
        filterRequest.setPageNum("1");
        filterRequest.setPageSize("258");
        filterRequest.setTreeFiltering("ONELEVEL");

        return filterRequest;
    }
}
